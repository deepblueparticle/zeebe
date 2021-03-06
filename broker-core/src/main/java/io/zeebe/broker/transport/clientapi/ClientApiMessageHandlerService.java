/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.transport.clientapi;

import io.zeebe.broker.clustering.base.partitions.Partition;
import io.zeebe.dispatcher.Dispatcher;
import io.zeebe.servicecontainer.*;

public class ClientApiMessageHandlerService implements Service<ClientApiMessageHandler> {
  private final Injector<Dispatcher> controlMessageBufferInjector = new Injector<>();
  protected ClientApiMessageHandler service;

  protected final ServiceGroupReference<Partition> leaderPartitionsGroupReference =
      ServiceGroupReference.<Partition>create()
          .onAdd((name, partition) -> service.addPartition(partition))
          .onRemove((name, partition) -> service.removePartition(partition))
          .build();

  @Override
  public void start(ServiceStartContext startContext) {
    final Dispatcher controlMessageBuffer = controlMessageBufferInjector.getValue();
    service = new ClientApiMessageHandler(controlMessageBuffer);
  }

  @Override
  public void stop(ServiceStopContext arg0) {
    // nothing to do
  }

  @Override
  public ClientApiMessageHandler get() {
    return service;
  }

  public Injector<Dispatcher> getControlMessageBufferInjector() {
    return controlMessageBufferInjector;
  }

  public ServiceGroupReference<Partition> getLeaderParitionsGroupReference() {
    return leaderPartitionsGroupReference;
  }
}
