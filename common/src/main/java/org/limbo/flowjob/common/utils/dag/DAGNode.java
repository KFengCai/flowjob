/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.common.utils.dag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
public class DAGNode implements Serializable {

    private static final long serialVersionUID = 8572090796475782411L;

    protected String id;

    protected Set<String> parentIds;

    protected Set<String> childrenIds;

    @DAGNodeIgnoreField
    protected int status = DAG.STATUS_INIT;

    @JsonCreator
    public DAGNode(@JsonProperty("id") String id, @JsonProperty("childrenIds") Set<String> childrenIds) {
        this.id = id;
        this.childrenIds = CollectionUtils.isEmpty(childrenIds) ? Collections.emptySet() : childrenIds;
        this.parentIds = new HashSet<>();
    }

    public void addParent(String childrenId) {
        parentIds.add(childrenId);
    }

}
