package com.grpc.demo.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.Http2Flags;

/**
 * Created by yeyc on 2016/11/25.
 */
public class FrameEntity {
    private int payloadLength ;
    private byte frameType;
    private Http2Flags flags;
    private int streamId;
    private ByteBuf payload;

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public byte getFrameType() {
        return frameType;
    }

    public void setFrameType(byte frameType) {
        this.frameType = frameType;
    }

    public Http2Flags getFlags() {
        return flags;
    }

    public void setFlags(Http2Flags flags) {
        this.flags = flags;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }
}
