package com.syj.tcpentrypoint.msg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.syj.tcpentrypoint.util.Constants;

/**
 * 
*  @des    :The message header
 * @author:shenyanjun1
 * @date   :2018-12-14 17:05
 */
public class MessageHeader implements Cloneable{

    private Integer length; // 总长度 包含magiccode + header + body

    private Short headerLength;
    private int msgType;
    private int msgId;
    private Map<Byte,Object> keysMap = new ConcurrentHashMap<Byte,Object>();

    public Map<Byte,Object> getAttrMap(){
        return this.keysMap;
    }

	public MessageHeader setValues(int msgType, int msgId) {
		this.msgId = msgId;
		this.msgType = msgType;
		return this;
	}

    public MessageHeader copyHeader(MessageHeader header){
        this.msgId = header.msgId;
        this.msgType = header.msgType;
        this.length = header.getLength();
        this.headerLength = header.getHeaderLength();
        Map<Byte,Object> tempMap = header.getAttrMap();
        for(Map.Entry<Byte,Object> entry:tempMap.entrySet()){
            this.keysMap.put(entry.getKey(),entry.getValue());
        }
        return this;
    }

    public Short getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(Short headerLength) {
        this.headerLength = headerLength;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
    
    public void addHeadKey(Constants.HeadKey key, Object value) {
        if (!key.getType().isInstance(value)) { // 检查类型
            throw new IllegalArgumentException("type mismatch of key:" + key.getNum() + ", expect:"
                    + key.getType().getName() + ", actual:" + value.getClass().getName());
        }
        keysMap.put(key.getNum(), value);
    }

    public Object removeByKey(Constants.HeadKey key){
        return keysMap.remove(key.getNum());
    }

    public Object getAttrByKey(Constants.HeadKey key){
        return keysMap.get(key.getNum());

    }

    public void setValuesInKeyMap(Map<Byte,Object> valueMap){
        this.keysMap.putAll(valueMap);

    }

    public int getAttrMapSize(){
        int mapSize = keysMap.size();
        return mapSize;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageHeader)) return false;

        MessageHeader that = (MessageHeader) o;

        if (headerLength != null ? !headerLength.equals(that.headerLength) : that.headerLength != null) return false;
        if (msgId != that.msgId) return false;
        if (msgType != that.msgType) return false;
        if (length != null ? !length.equals(that.length) : that.length != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = msgId;
        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + msgType;
        result = 31 * result + (headerLength != null ? headerLength.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String keymapStr = "";
        for(Map.Entry<Byte,Object> entry:keysMap.entrySet()){
            keymapStr = keymapStr+" "+ entry.getKey().toString()+" : "+entry.getValue().toString();
        }

        return "MessageHeader{" +
                "msgId=" + msgId +
                ", length=" + length +
                ", msgType=" + msgType +
                ", headerLength=" + headerLength +
                ", keysMap=" + keymapStr +
                "}";
    }

    /**
     * 克隆后和整体原来不是一个对象，
     * 属性相同，修改当前属性不会改变原来的
     * map和原来是一个对象，修改当前map也会改原来的
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
	public MessageHeader clone() {
        MessageHeader header = null;
        try {
            header = (MessageHeader) super.clone();
        } catch (CloneNotSupportedException e) {
            header = new MessageHeader();
            header.copyHeader(this);
        }
        return header;
    }
}