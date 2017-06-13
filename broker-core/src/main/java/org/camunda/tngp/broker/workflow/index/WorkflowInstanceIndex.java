/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.tngp.broker.workflow.index;

import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;

import java.nio.ByteOrder;

import org.agrona.concurrent.UnsafeBuffer;
import org.camunda.tngp.broker.logstreams.processor.HashIndexSnapshotSupport;
import org.camunda.tngp.hashindex.Long2BytesHashIndex;
import org.camunda.tngp.hashindex.store.IndexStore;
import org.camunda.tngp.logstreams.spi.SnapshotSupport;

/**
 * Index that maps <b>workflow instance key</b> to
 *
 * <li>workflow instance event position
 * <li>active token count
 * <li>activity instance key
 */
public class WorkflowInstanceIndex
{
    private static final int INDEX_VALUE_SIZE = SIZE_OF_LONG + SIZE_OF_INT + SIZE_OF_LONG;

    private static final int POSITION_OFFSET = 0;
    private static final int TOKEN_COUNT_OFFSET = POSITION_OFFSET + SIZE_OF_LONG;
    private static final int ACTIVITY_INSTANCE_KEY_OFFSET = TOKEN_COUNT_OFFSET + SIZE_OF_INT;

    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[INDEX_VALUE_SIZE]);

    private final Long2BytesHashIndex index;
    private final HashIndexSnapshotSupport<Long2BytesHashIndex> snapshotSupport;

    private long key;
    private boolean isRead = false;

    public WorkflowInstanceIndex(final IndexStore indexStore)
    {
        this.index = new Long2BytesHashIndex(indexStore, Short.MAX_VALUE, 256, INDEX_VALUE_SIZE);
        this.snapshotSupport = new HashIndexSnapshotSupport<>(index, indexStore);
    }

    public SnapshotSupport getSnapshotSupport()
    {
        return snapshotSupport;
    }

    public void reset()
    {
        isRead = false;
    }

    public void remove(long workflowInstanceKey)
    {
        index.remove(workflowInstanceKey);
    }

    public WorkflowInstanceIndex wrapWorkflowInstanceKey(long key)
    {
        isRead = false;

        final byte[] indexValue = index.get(key);
        if (indexValue != null)
        {
            buffer.wrap(indexValue);
            this.key = key;
            isRead = true;
        }

        return this;
    }

    public long getPosition()
    {
        return isRead ? buffer.getLong(POSITION_OFFSET, BYTE_ORDER) : -1L;
    }

    public int getTokenCount()
    {
        return isRead ? buffer.getInt(TOKEN_COUNT_OFFSET, BYTE_ORDER) : -1;
    }

    public long getActivityInstanceKey()
    {
        return isRead ? buffer.getLong(ACTIVITY_INSTANCE_KEY_OFFSET, BYTE_ORDER) : -1L;
    }

    public WorkflowInstanceIndex newWorkflowInstance(long workflowInstanceKey)
    {
        key = workflowInstanceKey;
        isRead = true;
        return this;
    }

    public void write()
    {
        ensureRead();
        index.put(key, buffer.byteArray());
    }

    public WorkflowInstanceIndex setPosition(long position)
    {
        ensureRead();
        buffer.putLong(POSITION_OFFSET, position, BYTE_ORDER);
        return this;
    }

    public WorkflowInstanceIndex setActivityKey(long activityInstanceKey)
    {
        ensureRead();
        buffer.putLong(ACTIVITY_INSTANCE_KEY_OFFSET, activityInstanceKey, BYTE_ORDER);
        return this;
    }

    public WorkflowInstanceIndex setActiveTokenCount(int activeTokenCount)
    {
        ensureRead();
        buffer.putInt(TOKEN_COUNT_OFFSET, activeTokenCount, BYTE_ORDER);
        return this;
    }

    private void ensureRead()
    {
        if (!isRead)
        {
            throw new IllegalStateException("must call wrap() before");
        }
    }

}