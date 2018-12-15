/*
 * Copyright © 2018 Cask Data, Inc.
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

package co.cask.cdap.spi.data.table;

import co.cask.cdap.spi.data.StructuredTable;

import java.util.Objects;

/**
 * Specifies the Id of a {@link StructuredTable}.
 */
public class TableId {
  private final String name;

  /**
   * Convenience method to create the TableId.
   *
   * @param name table name, the name can only contain alpha-numeric characters and underscore.
   *             The name has to start with an alphabet.
   * @return the TableId for the given table name
   */
  public static TableId of(String name) {
    return new TableId(name);
  }

  private TableId(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TableId tableId = (TableId) o;
    return Objects.equals(name, tableId.name);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "TableId{" +
      "name='" + name + '\'' +
      '}';
  }
}