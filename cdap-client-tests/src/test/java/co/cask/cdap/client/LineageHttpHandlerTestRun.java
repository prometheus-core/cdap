/*
 * Copyright © 2016-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.client;

import co.cask.cdap.api.metadata.MetadataScope;
import co.cask.cdap.client.app.AllProgramsApp;
import co.cask.cdap.common.BadRequestException;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.metadata.MetadataRecord;
import co.cask.cdap.common.utils.Tasks;
import co.cask.cdap.data2.metadata.lineage.AccessType;
import co.cask.cdap.data2.metadata.lineage.Lineage;
import co.cask.cdap.data2.metadata.lineage.LineageSerializer;
import co.cask.cdap.data2.metadata.lineage.Relation;
import co.cask.cdap.proto.NamespaceMeta;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.ProgramStatus;
import co.cask.cdap.proto.RunRecord;
import co.cask.cdap.proto.id.ApplicationId;
import co.cask.cdap.proto.id.DatasetId;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.id.ProgramId;
import co.cask.cdap.proto.metadata.lineage.CollapseType;
import co.cask.cdap.proto.metadata.lineage.LineageRecord;
import co.cask.cdap.test.SlowTests;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.twill.api.RunId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Tests lineage recording and query.
 */
@Category(SlowTests.class)
public class LineageHttpHandlerTestRun extends MetadataTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(LineageHttpHandlerTestRun.class);

  @Test
  public void testAllProgramsLineage() throws Exception {
    NamespaceId namespace = new NamespaceId("testAllProgramsLineage");
    ApplicationId app = namespace.app(AllProgramsApp.NAME);
    ProgramId mapreduce = app.mr(AllProgramsApp.NoOpMR.NAME);
    ProgramId mapreduce2 = app.mr(AllProgramsApp.NoOpMR2.NAME);
    ProgramId spark = app.spark(AllProgramsApp.NoOpSpark.NAME);
    ProgramId service = app.service(AllProgramsApp.NoOpService.NAME);
    ProgramId worker = app.worker(AllProgramsApp.NoOpWorker.NAME);
    ProgramId workflow = app.workflow(AllProgramsApp.NoOpWorkflow.NAME);
    DatasetId dataset = namespace.dataset(AllProgramsApp.DATASET_NAME);
    DatasetId dataset2 = namespace.dataset(AllProgramsApp.DATASET_NAME2);
    DatasetId dataset3 = namespace.dataset(AllProgramsApp.DATASET_NAME3);

    namespaceClient.create(new NamespaceMeta.Builder().setName(namespace.getNamespace()).build());
    try {
      appClient.deploy(namespace, createAppJarFile(AllProgramsApp.class));

      // Add metadata
      ImmutableSet<String> sparkTags = ImmutableSet.of("spark-tag1", "spark-tag2");
      addTags(spark, sparkTags);
      Assert.assertEquals(sparkTags, getTags(spark, MetadataScope.USER));

      ImmutableSet<String> workerTags = ImmutableSet.of("worker-tag1");
      addTags(worker, workerTags);
      Assert.assertEquals(workerTags, getTags(worker, MetadataScope.USER));

      ImmutableMap<String, String> datasetProperties = ImmutableMap.of("data-key1", "data-value1");
      addProperties(dataset, datasetProperties);
      Assert.assertEquals(datasetProperties, getProperties(dataset, MetadataScope.USER));

      // Start all programs
      RunId mrRunId = runAndWait(mapreduce);
      RunId mrRunId2 = runAndWait(mapreduce2);
      RunId sparkRunId = runAndWait(spark);
      runAndWait(workflow);
      RunId workflowMrRunId = getRunId(mapreduce, mrRunId);
      RunId serviceRunId = runAndWait(service);
      // Worker makes a call to service to make it access datasets,
      // hence need to make sure service starts before worker, and stops after it.
      RunId workerRunId = runAndWait(worker);

      // Wait for programs to finish
      waitForStop(mapreduce, false);
      waitForStop(mapreduce2, false);
      waitForStop(spark, false);
      waitForStop(workflow, false);
      waitForStop(worker, false);
      waitForStop(service, true);

      long now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
      long oneHour = TimeUnit.HOURS.toSeconds(1);

      // Fetch dataset lineage
      LineageRecord lineage = fetchLineage(dataset, now - oneHour, now + oneHour, toSet(CollapseType.ACCESS), 10);

      // dataset is accessed by all programs
      LineageRecord expected =
        LineageSerializer.toLineageRecord(
          now - oneHour,
          now + oneHour,
          new Lineage(ImmutableSet.of(
            // Dataset access
            new Relation(dataset, mapreduce, AccessType.WRITE, mrRunId),
            new Relation(dataset3, mapreduce, AccessType.READ, mrRunId),
            new Relation(dataset, mapreduce2, AccessType.WRITE, mrRunId2),
            new Relation(dataset2, mapreduce2, AccessType.READ, mrRunId2),
            new Relation(dataset, spark, AccessType.READ, sparkRunId),
            new Relation(dataset2, spark, AccessType.WRITE, sparkRunId),
            new Relation(dataset3, spark, AccessType.READ, sparkRunId),
            new Relation(dataset3, spark, AccessType.WRITE, sparkRunId),
            new Relation(dataset, mapreduce, AccessType.WRITE, workflowMrRunId),
            new Relation(dataset3, mapreduce, AccessType.READ, workflowMrRunId),
            new Relation(dataset, service, AccessType.WRITE, serviceRunId),
            new Relation(dataset, worker, AccessType.WRITE, workerRunId)
          )),
          toSet(CollapseType.ACCESS));
      Assert.assertEquals(expected, lineage);

      // Assert metadata
      // Id.Worker needs conversion to Id.Program JIRA - CDAP-3658
      ProgramId programForWorker = new ProgramId(worker.getNamespace(), worker.getApplication(), worker.getType(),
                                                 worker.getEntityName());
      Assert.assertEquals(toSet(new MetadataRecord(app, MetadataScope.USER, emptyMap(), emptySet()),
                                new MetadataRecord(programForWorker, MetadataScope.USER, emptyMap(),
                                                   workerTags),
                                new MetadataRecord(dataset, MetadataScope.USER, datasetProperties,
                                                   emptySet())),
                          getMetadataForRun(worker.run(workerRunId.getId()).toMetadataEntity()));

      // Id.Spark needs conversion to Id.Program JIRA - CDAP-3658
      ProgramId programForSpark = new ProgramId(spark.getNamespace(), spark.getApplication(), spark.getType(),
                                                spark.getEntityName());
      Assert.assertEquals(toSet(new MetadataRecord(app, MetadataScope.USER, emptyMap(), emptySet()),
                                new MetadataRecord(programForSpark, MetadataScope.USER, emptyMap(),
                                                   sparkTags),
                                new MetadataRecord(dataset, MetadataScope.USER, datasetProperties,
                                                   emptySet()),
                                new MetadataRecord(dataset2, MetadataScope.USER, emptyMap(), emptySet()),
                                new MetadataRecord(dataset3, MetadataScope.USER, emptyMap(), emptySet())),
                          getMetadataForRun(spark.run(sparkRunId.getId()).toMetadataEntity()));
    } finally {
      namespaceClient.delete(namespace);
    }
  }

  @Test
  public void testLineageInNonExistingNamespace() throws Exception {
    NamespaceId namespace = new NamespaceId("nonExistent");
    ApplicationId app = namespace.app(AllProgramsApp.NAME);
    ProgramId mr = app.mr(AllProgramsApp.NoOpMR.NAME);
    assertRunMetadataNotFound(mr.run(RunIds.generate(1000).getId()));
  }

  @Test
  public void testLineageForNonExistingEntity() throws Exception {
    DatasetId datasetInstance = NamespaceId.DEFAULT.dataset("dummy");
    fetchLineage(datasetInstance, -100, 200, 10, BadRequestException.class);
    fetchLineage(datasetInstance, 100, -200, 10, BadRequestException.class);
    fetchLineage(datasetInstance, 200, 100, 10, BadRequestException.class);
    fetchLineage(datasetInstance, 100, 200, -10, BadRequestException.class);
  }

  private RunId runAndWait(final ProgramId program) throws Exception {
    LOG.info("Starting program {}", program);
    programClient.start(program);
    return getRunId(program);
  }

  private void waitForStop(ProgramId program, boolean needsStop) throws Exception {
    if (needsStop && !programClient.getStatus(program).equals(ProgramStatus.STOPPED.toString())) {
      LOG.info("Stopping program {}", program);
      programClient.stop(program);
    }
    programClient.waitForStatus(program, ProgramStatus.STOPPED, 15, TimeUnit.SECONDS);
    LOG.info("Program {} has stopped", program);
  }

  private RunId getRunId(ProgramId program) throws Exception {
    return getRunId(program, null);
  }

  private RunId getRunId(final ProgramId program, @Nullable final RunId exclude) throws Exception {
    AtomicReference<Iterable<RunRecord>> runRecords = new AtomicReference<>();
    Tasks.waitFor(1, () -> {
      runRecords.set(
        programClient.getProgramRuns(program, ProgramRunStatus.ALL.name(), 0, Long.MAX_VALUE, Integer.MAX_VALUE)
          .stream()
          .filter(record -> (exclude == null || !record.getPid().equals(exclude.getId())) &&
            record.getStatus() != ProgramRunStatus.PENDING)
          .collect(Collectors.toList()));
      return Iterables.size(runRecords.get());
    }, 60, TimeUnit.SECONDS, 10, TimeUnit.MILLISECONDS);
    Assert.assertEquals(1, Iterables.size(runRecords.get()));
    return RunIds.fromString(Iterables.getFirst(runRecords.get(), null).getPid());
  }

  @SafeVarargs
  private static <T> Set<T> toSet(T... elements) {
    return ImmutableSet.copyOf(elements);
  }

  private Set<String> emptySet() {
    return Collections.emptySet();
  }

  private Map<String, String> emptyMap() {
    return Collections.emptyMap();
  }
}
