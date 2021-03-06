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

import * as React from 'react';
import T from 'i18n-react';
import { getCurrentNamespace } from 'services/NamespaceStore';
import { humanReadableDate } from 'services/helpers';
import DraftActions from 'components/PipelineList/DraftPipelineView/DraftActions';
import { IDraft } from 'components/PipelineList/DraftPipelineView/types';

interface IProps {
  draft: IDraft;
}

const PREFIX = 'features.PipelineList';

export default class DraftTableRow extends React.PureComponent<IProps> {
  public render() {
    const draft = this.props.draft;
    const namespace = getCurrentNamespace();
    const lastSaved = humanReadableDate(draft.__ui__.lastSaved, true);

    const link = window.getHydratorUrl({
      stateName: 'hydrator.create',
      stateParams: {
        namespace,
        draftId: draft.__ui__.draftId,
      },
    });

    return (
      <a href={link} className="grid-row">
        <div className="name">
          <h5>{draft.name}</h5>
        </div>
        <div className="type">{T.translate(`${PREFIX}.${draft.artifact.name}`)}</div>
        <div className="last-saved">{lastSaved}</div>

        <DraftActions draft={this.props.draft} />
      </a>
    );
  }
}
