/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.logstreams.impl.service;

import io.zeebe.logstreams.impl.snapshot.fs.FsSnapshotStorage;
import io.zeebe.logstreams.impl.snapshot.fs.FsSnapshotStorageConfiguration;
import io.zeebe.logstreams.spi.SnapshotStorage;
import io.zeebe.servicecontainer.*;

public class FsSnapshotStorageService implements Service<SnapshotStorage> {
  private final FsSnapshotStorageConfiguration config;

  private FsSnapshotStorage service;

  public FsSnapshotStorageService(FsSnapshotStorageConfiguration snapshotStorageConfiguration) {
    this.config = snapshotStorageConfiguration;
  }

  @Override
  public void start(ServiceStartContext startContext) {
    service = new FsSnapshotStorage(config);
  }

  @Override
  public void stop(ServiceStopContext stopContext) {
    // no-op
  }

  @Override
  public SnapshotStorage get() {
    return service;
  }
}
