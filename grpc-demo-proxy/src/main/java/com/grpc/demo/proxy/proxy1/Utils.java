package com.grpc.demo.proxy.proxy1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.*;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.handler.codec.http2.Http2CodecUtil.readUnsignedInt;
import static io.netty.handler.codec.http2.Http2Error.ENHANCE_YOUR_CALM;
import static io.netty.handler.codec.http2.Http2Error.FRAME_SIZE_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Exception.streamError;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * Created by yeyc on 2016/11/21.
 */
public class Utils {

    public static String formatByteBuf(ChannelHandlerContext ctx, String eventName, ByteBuf msg) {
        String chStr = ctx.channel().toString();
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

    public static Http2Headers readHeadersFrame(final ChannelHandlerContext ctx, ByteBuf byteBuf) throws Http2Exception {

        int payloadLength = byteBuf.readUnsignedMedium();
        byte frameType = byteBuf.readByte();
        Http2Flags flags = new Http2Flags(byteBuf.readUnsignedByte());
        int streamId = readUnsignedInt(byteBuf);
        System.out.println("header    streamId:"+streamId);

        ByteBuf payload = byteBuf.readSlice(payloadLength);

        final int padding;
        if (!flags.paddingPresent()) {
            padding =  0;
        }
        else {
            padding = payload.readUnsignedByte() + 1;
        }

        if (flags.priorityPresent()) {
            long word1 = payload.readUnsignedInt();
            final boolean exclusive = (word1 & 0x80000000L) != 0;
            final int streamDependency = (int) (word1 & 0x7FFFFFFFL);
            final short weight = (short) (payload.readUnsignedByte() + 1);
            final ByteBuf fragment = payload.readSlice(lengthWithoutTrailingPadding(payload.readableBytes(), padding));


            final HeadersBlockBuilder hdrBlockBuilder = new HeadersBlockBuilder();
            hdrBlockBuilder.addFragment(fragment, ctx.alloc(), flags.endOfHeaders());
            return  hdrBlockBuilder.headers();

        }
        return null;
    }


    public static String readDataFrame( ByteBuf byteBuf) throws Http2Exception {
        int payloadLength = byteBuf.readUnsignedMedium();
        byte frameType = byteBuf.readByte();
        Http2Flags flags = new Http2Flags(byteBuf.readUnsignedByte());
        int streamId = readUnsignedInt(byteBuf);

        System.out.println("data    streamId:"+streamId);

        ByteBuf payload = byteBuf.readSlice(payloadLength);


        final int padding;
        if (!flags.paddingPresent()) {
            padding =  0;
        }
        else {
            padding = payload.readUnsignedByte() + 1;
        }

        int dataLength = lengthWithoutTrailingPadding(payload.readableBytes(), padding);
        if (dataLength < 0) {
            throw streamError(streamId, FRAME_SIZE_ERROR,
                    "Frame payload too small for padding.");
        }

        ByteBuf data = payload.readSlice(dataLength);

        byte[] b = new byte[data.readableBytes()];
        data.readBytes(b);
        return new String(b);
    }


    private static int lengthWithoutTrailingPadding(int readableBytes, int padding) {
        return padding == 0
                ? readableBytes
                : readableBytes - (padding - 1);
    }


    static class HeadersBlockBuilder {
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
