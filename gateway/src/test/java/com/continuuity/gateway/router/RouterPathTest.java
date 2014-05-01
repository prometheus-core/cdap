package com.continuuity.gateway.router;

import com.continuuity.common.conf.Constants;
import com.continuuity.gateway.GatewayFastTestsSuite;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  To test the RouterPathLookup regular expression tests.
 */
public class RouterPathTest {

  private static RouterPathLookup pathLookup;
  private final String VERSION = "HTTP/1.1";
  private static final String API_KEY = "SampleTestApiKey";

  @BeforeClass
  public static void init() throws Exception {
    pathLookup = GatewayFastTestsSuite.getInjector().getInstance(RouterPathLookup.class);
  }

  @Test
  public void testMetricsPath() throws Exception {
    //Following URIs might not give actual results but we want to test resilience of Router Path Lookup
    String flowPath = "/v2///metrics/reactor/apps/InvalidApp?cache=true";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    String result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.METRICS, result);

    flowPath = "/v2/metrics";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("DELETE"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.METRICS, result);

    flowPath = "/v2/metrics?cache=true";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("POST"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.METRICS, result);
  }

  @Test
  public void testLogPath() throws Exception {
    //Following URIs might not give actual results but we want to test resilience of Router Path Lookup
    String flowPath = "/v2/apps//InvalidApp///procedures/ProcName/logs?start=10";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    String result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.METRICS, result);

    flowPath = "///v2///apps/InvalidApp/flows/FlowName/////logs?stop=10";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("POST"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.METRICS, result);

    flowPath = "v2/apps/InvalidApp/procedures/ProName/logs/abcd?stop=10";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("DELETE"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.METRICS, result);
  }

  @Test
  public void testProcedurePath() throws Exception {
    //Following URIs might not give actual results but we want to test resilience of Router Path Lookup
    //Procedure Path /v2/apps/<appid>/procedures/<procedureid>/methods/<methodName>
    //Discoverable Service Name -> procedure.%s.%s.%s", accountId, appId, procedureName;
    String accId = "developer";

    String flowPath = "/v2/apps//InvalidApp///procedures/ProcName///methods//H?user=asd";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    httpRequest.setHeader(Constants.Gateway.CONTINUUITY_API_KEY, API_KEY);
    String result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals("procedure." + accId + ".InvalidApp.ProcName", result);

    flowPath = "///v2///apps/Invali_-123//procedures/Hel123@!@!//methods/Asdad?das////";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("POST"), flowPath);
    httpRequest.setHeader(Constants.Gateway.CONTINUUITY_API_KEY, API_KEY);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals("procedure." + accId + ".Invali_-123.Hel123@!@!", result);

    flowPath = "v2/apps/InvalidApp/procedures/ProName/methods/getCustomer";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    httpRequest.setHeader(Constants.Gateway.CONTINUUITY_API_KEY, API_KEY);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals("procedure." + accId + ".InvalidApp.ProName", result);
  }

  @Test
  public void testStreamPath() throws Exception {
    //Following URIs might not give actual results but we want to test resilience of Router Path Lookup
    String flowPath = "/v2/streams?cache=true";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    String result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);

    flowPath = "///v2/streams///";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("POST"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);

    flowPath = "v2///streams///";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("PUT"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);

    flowPath = "//v2///streams/HelloStream//flows///";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);

    flowPath = "//v2///streams/HelloStream//flows///";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("DELETE"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.STREAM_HANDLER, result);

    flowPath = "//v2///streams/HelloStream//flows///";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("POST"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.STREAM_HANDLER, result);

    flowPath = "v2//streams//flows///";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("DELETE"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.STREAM_HANDLER, result);

    flowPath = "v2//streams/InvalidStreamName/flows/";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);

    flowPath = "v2//streams/InvalidStreamName/flows/";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("DELETE"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.STREAM_HANDLER, result);

    flowPath = "v2//streams/InvalidStreamName/info/";
    httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.STREAM_HANDLER, result);
  }

  @Test
  public void testRouterFlowPathLookUp() throws Exception {
    String flowPath = "/v2//apps/ResponseCodeAnalytics/flows/LogAnalyticsFlow/status";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    String result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);
  }

  @Test
  public void testRouterWorkFlowPathLookUp() throws Exception {
    String procPath = "/v2/apps///PurchaseHistory///workflows/PurchaseHistoryWorkflow/status";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

  @Test
  public void testRouterProcedurePathLookUp() throws Exception {
    String procPath = "/v2//apps/ResponseCodeAnalytics/procedures/StatusCodeProcedure/status";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

  @Test
  public void testRouterDeployPathLookUp() throws Exception {
    String procPath = "/v2//apps/";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("PUT"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

  @Test
  public void testRouterFlowletInstancesLookUp() throws Exception {
    String procPath = "/v2//apps/WordCount/flows/WordCountFlow/flowlets/StreamSource/instances";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("PUT"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

}
