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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cn.songm.common.utils.JsonUtils;
import cn.songm.common.utils.Sequence;
import cn.songm.common.utils.StringUtils;
import cn.songm.im.handler.Handler.Operation;
import cn.songm.im.model.message.Message;
import io.netty.channel.Channel;

/**
 * 用户与服务端的会话
 *
 * @author zhangsong
 * @since 0.1, 2016-7-29
 * @version 0.1
 * 
 */
public class SessionCh extends Session {

    private static final long serialVersionUID = -2720707956157888183L;

    private Set<Channel> chSet;

    public SessionCh(String sessionId, Token token) {
        super(sessionId, token);
        chSet = new HashSet<Channel>();
    }

    public void addCh(Channel ch) {
        if (ch instanceof ChLongPolling) {
            ChLongPolling clp = (ChLongPolling) ch;
            if (isFirstConn(clp.getChId())) {
                clp.setChId(Sequence.getInstance().getSequence(7));
                chSet.add(ch);
            }
        } else {
            chSet.add(ch);
        }
    }

    public boolean isFirstConn(String chId) {
        if (StringUtils.isEmptyOrNull(chId)) {
            return true;
        }
        if (getChannel(chId) == null) {
            return true;
        }
        return false;
    }

    public void removeChannel(Channel ch) {
        if (ch instanceof ChLongPolling) {
            ChLongPolling clp = (ChLongPolling) ch;
            clp.clearResMsg();
        } else {
            ch.close().syncUninterruptibly();
        }
        chSet.remove(ch);
    }

    public ChLongPolling getChannel(String chId) {
        Iterator<Channel> iter = chSet.iterator();
        while (iter.hasNext()) {
            Channel ch = iter.next();
            if (ch instanceof ChLongPolling) {
                ChLongPolling clp = (ChLongPolling) ch;
                if (clp.getChId().equals(chId)) {
                    return clp;
                }
            }
        }
        return null;
    }

    public void onReceived(byte[] payload, Channel out) {
        Message msg = JsonUtils.fromJson(payload, Message.class);
        Result<Message> res = new Result<Message>();
        res.setData(msg);
        
        Iterator<Channel> iter = chSet.iterator();
        while (iter.hasNext()) {
            Channel ch = iter.next();
            if (ch == out)
                continue;
            if (ch instanceof ChLongPolling) {
                ChLongPolling clp = (ChLongPolling) ch;
                clp.addResMsg(JsonUtils.toJsonBytes(res, res.getClass()));
            } else {
                Protocol pro = new Protocol();
                pro.setOperation(Operation.BROKER_MSG.getValue());
                pro.setBody(JsonUtils.toJsonBytes(res, res.getClass()));
                ch.writeAndFlush(pro);
            }
        }
    }

    public void clearChannels() {
        Iterator<Channel> iter = chSet.iterator();
        while (iter.hasNext()) {
            Channel ch = (Channel) iter.next();
            if (ch instanceof ChLongPolling) {
                ChLongPolling clp = (ChLongPolling) ch;
                clp.clearResMsg();
            } else {
                ch.close().syncUninterruptibly();
            }
        }
        chSet.clear();
    }

    public boolean isChannels() {
        return !chSet.isEmpty();
    }
}
