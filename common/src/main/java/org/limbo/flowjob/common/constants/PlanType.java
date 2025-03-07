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

package org.limbo.flowjob.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 计划类型
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum PlanType {
    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),
    SINGLE(1, "单任务"),
    WORKFLOW(2, "工作流任务"),
    ;

    @JsonValue
    public final byte status;

    public final String desc;

    @JsonCreator
    PlanType(int status, String desc) {
        this(((byte) status), desc);
    }

    PlanType(byte status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     *
     * @param status 待校验状态值
     */
    public boolean is(PlanType status) {
        return equals(status);
    }

    /**
     * 校验是否是当前状态
     *
     * @param status 待校验状态值
     */
    public boolean is(Number status) {
        return status != null && status.byteValue() == this.status;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static PlanType parse(Number status) {
        if (status == null) {
            return UNKNOWN;
        }

        for (PlanType statusEnum : values()) {
            if (statusEnum.is(status)) {
                return statusEnum;
            }
        }

        return UNKNOWN;
    }

}
