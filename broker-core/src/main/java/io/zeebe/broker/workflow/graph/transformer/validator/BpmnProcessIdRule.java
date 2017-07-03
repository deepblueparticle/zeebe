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
package io.zeebe.broker.workflow.graph.transformer.validator;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResultCollector;
import io.zeebe.broker.workflow.graph.transformer.BpmnTransformer;

public class BpmnProcessIdRule implements ModelElementValidator<Process>
{
    @Override
    public Class<Process> getElementType()
    {
        return Process.class;
    }

    @Override
    public void validate(Process process, ValidationResultCollector validationResultCollector)
    {
        final String bpmnProcessId = process.getId();

        if (bpmnProcessId == null || bpmnProcessId.isEmpty())
        {
            validationResultCollector.addError(ValidationCodes.MISSING_ID, "BPMN process id is required.");
        }
        else if (bpmnProcessId.length() > BpmnTransformer.ID_MAX_LENGTH)
        {
            validationResultCollector.addError(ValidationCodes.ID_TOO_LONG,
                    String.format("BPMN process id must not be longer than %d.", BpmnTransformer.ID_MAX_LENGTH));
        }
    }

}