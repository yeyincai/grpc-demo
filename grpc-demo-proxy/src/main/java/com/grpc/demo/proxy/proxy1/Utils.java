package com.grpc.demo.proxy.proxy1;

import com.grpc.demo.proxy.FrameEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.*;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.handler.codec.http2.Http2CodecUtil.*;
import static io.netty.handler.codec.http2.Http2Error.*;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Exception.streamError;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * Created by yeyc on 2016/11/21.
 */
public class Utils {

    public static final String UPGRADE_RESPONSE_HEADER = "http-to-http2-upgrade";

    public static String formatByteBuf(String eventName, ByteBuf msg) {
        String chStr = "cxf:";
        int length = msg.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(": 0B");
            return buf.toString();
        } else {
            int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
            StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + 10 + 1 + 2 + rows * 80);

            buf.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B').append(NEWLINE);
            appendPrettyHexDump(buf, msg);

            return buf.toString();
        }
    }


    private static final Http2HeadersDecoder headersDecoder = new DefaultHttp2HeadersDecoder(true);

    public static FrameEntity decodeFrameEntity(ByteBuf byteBuf) {
        final FrameEntity frameEntity = new FrameEntity();

        final int payloadLength = byteBuf.readUnsignedMedium();
        final byte frameType = byteBuf.readByte();
        final Http2Flags flags = new Http2Flags(byteBuf.readUnsignedByte());
        final int streamId = readUnsignedInt(byteBuf);
        final ByteBuf payload = byteBuf.readSlice(payloadLength);

        frameEntity.setPayloadLength(payloadLength);
        frameEntity.setFrameType(frameType);
        frameEntity.setFlags(flags);
        frameEntity.setStreamId(streamId);
        frameEntity.setPayload(payload);
        return frameEntity;
    }

    public static Http2Headers readHeadersFrame(final ChannelHandlerContext ctx, FrameEntity frameEntity) throws Http2Exception {

        final ByteBuf payload = frameEntity.getPayload().copy();
        final Http2Flags flags = frameEntity.getFlags();
        final int padding = getPadding(flags.paddingPresent(), payload);

        if (flags.priorityPresent()) {
            long word1 = payload.readUnsignedInt();
            final boolean exclusive = (word1 & 0x80000000L) != 0;
            final int streamDependency = (int) (word1 & 0x7FFFFFFFL);
            final short weight = (short) (payload.readUnsignedByte() + 1);
            final ByteBuf fragment = payload.readSlice(lengthWithoutTrailingPadding(payload.readableBytes(), padding));
            final HeadersBlockBuilder hdrBlockBuilder = new HeadersBlockBuilder();
            hdrBlockBuilder.addFragment(fragment, ctx.alloc(), flags.endOfHeaders());
            return hdrBlockBuilder.headers();

        }
        return null;
    }


    public static String readDataFrame(FrameEntity frameEntity) throws Http2Exception {
        final ByteBuf payload = frameEntity.getPayload().copy();
        final Http2Flags flags = frameEntity.getFlags();
        final int padding = getPadding(flags.paddingPresent(), payload);

        int dataLength = lengthWithoutTrailingPadding(payload.readableBytes(), padding);
        if (dataLength < 0) {
            throw streamError(frameEntity.getStreamId(), FRAME_SIZE_ERROR, "Frame payload too small for padding.");
        }

        ByteBuf data = payload.readSlice(dataLength);

        byte[] b = new byte[data.readableBytes()];
        data.readBytes(b);
        return new String(b);
    }

    public static Http2Settings readSettingsFrame(FrameEntity frameEntity) throws Http2Exception {
        final  ByteBuf payload = frameEntity.getPayload().copy();
        final int numSettings = frameEntity.getPayloadLength() / SETTING_ENTRY_LENGTH;
        final Http2Settings settings = new Http2Settings();
        for (int index = 0; index < numSettings; ++index) {
            char id = (char) payload.readUnsignedShort();
            long value = payload.readUnsignedInt();
            try {
                settings.put(id, Long.valueOf(value));
            } catch (IllegalArgumentException e) {
                switch (id) {
                    case SETTINGS_MAX_FRAME_SIZE:
                        throw connectionError(FRAME_SIZE_ERROR, e, e.getMessage());
                    case SETTINGS_INITIAL_WINDOW_SIZE:
                        throw connectionError(FLOW_CONTROL_ERROR, e, e.getMessage());
                    default:
                        throw connectionError(PROTOCOL_ERROR, e, e.getMessage());
                }
            }
        }
        return settings;
    }

    private static int getPadding(boolean paddingPresent, ByteBuf payload) {
        final int padding;
        if (!paddingPresent) {
            padding = 0;
        } else {
            padding = payload.readUnsignedByte() + 1;
        }
        return padding;
    }


    private static int lengthWithoutTrailingPadding(int readableBytes, int padding) {
        return padding == 0
                ? readableBytes
                : readableBytes - (padding - 1);
    }


    private static class HeadersBlockBuilder {
        private ByteBuf headerBlock;


        private void headerSizeExceeded() throws Http2Exception {
            close();
            throw connectionError(ENHANCE_YOUR_CALM, "Header size exceeded max allowed size (%d)",
                    headersDecoder.configuration().maxHeaderSize());
        }


        final void addFragment(ByteBuf fragment, ByteBufAllocator alloc, boolean endOfHeaders) throws Http2Exception {
            if (headerBlock == null) {
                if (fragment.readableBytes() > headersDecoder.configuration().maxHeaderSize()) {
                    headerSizeExceeded();
                }
                if (endOfHeaders) {
                    headerBlock = fragment.retain();
                } else {
                    headerBlock = alloc.buffer(fragment.readableBytes());
                    headerBlock.writeBytes(fragment);
                }
                return;
            }
            if (headersDecoder.configuration().maxHeaderSize() - fragment.readableBytes() <
                    headerBlock.readableBytes()) {
                headerSizeExceeded();
            }
            if (headerBlock.isWritable(fragment.readableBytes())) {
                headerBlock.writeBytes(fragment);
            } else {
                ByteBuf buf = alloc.buffer(headerBlock.readableBytes() + fragment.readableBytes());
                buf.writeBytes(headerBlock);
                buf.writeBytes(fragment);
                headerBlock.release();
                headerBlock = buf;
            }
        }

        Http2Headers headers() throws Http2Exception {
            try {
                return headersDecoder.decodeHeaders(headerBlock);
            } finally {
                close();
            }
        }

        void close() {
            if (headerBlock != null) {
                headerBlock.release();
                headerBlock = null;
            }
        }
    }
}
