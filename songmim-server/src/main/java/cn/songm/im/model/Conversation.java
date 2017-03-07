/*
 * Copyright [2016] [zhangsong <songm.cn>].
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
 * 
 */
package cn.songm.im.model;

import java.io.Serializable;

/**
 * 会话
 *
 * @author zhangsong
 * @since 0.1, 2016-7-29
 * @version 0.1
 * 
 */
public class Conversation implements Serializable {

    private static final long serialVersionUID = -6267677672827188664L;

    public static enum Type {
        /** 私聊 */
        PRIVATE("private"),
        /** 群聊 */
        GROUP("group"),
        /** 通知 */
        NOTICE("notice");

        private String value;

        private Type(String v) {
            this.value = v;
        }
        
        public String getValue() {
            return this.value;
        }
        
        public static Type instance(String v) {
            for (Type t : Type.values()) {
                if (t.getValue().equals(v)) {
                    return t;
                }
            }
            throw new RuntimeException();
        }
    }
    
}
