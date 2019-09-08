/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;

import java.util.List;

/**
 * 以换行符为分割的解码器
 * A decoder that splits the received {@link ByteBuf}s on line endings.
 * <p>
 * Both {@code "\n"} and {@code "\r\n"} are handled.
 * <p>
 * The byte stream is expected to be in UTF-8 character encoding or ASCII. The current implementation
 * uses direct {@code byte} to {@code char} cast and then compares that {@code char} to a few low range
 * ASCII characters like {@code '\n'} or {@code '\r'}. UTF-8 is not using low range [0..0x7F]
 * byte values for multibyte codepoint representations therefore fully supported by this implementation.
 * <p>
 * For a more general delimiter-based decoder, see {@link DelimiterBasedFrameDecoder}.
 */
public class LineBasedFrameDecoder extends ByteToMessageDecoder {
    // 一行数据的最大长度（如果超过了这个长度是否需要丢弃，下面有个变量可配置）
    /** Maximum length of a frame we're willing to decode.  */
    private final int maxLength;
    // 如果一行数据超过了最大长度，是否需要抛出异常
    /** Whether or not to throw an exception as soon as we exceed maxLength. */
    private final boolean failFast;
    // 解析出的一行数据是否不需要携带换行符
    private final boolean stripDelimiter;

    // 是否需要丢弃数据
    /** True if we're discarding input because we're already over maxLength.  */
    private boolean discarding;
    // 解码器当现在已经丢弃了多少个字节
    private int discardedBytes;

    /** Last scan position. */
    private int offset;

    /**
     * Creates a new decoder.
     * @param maxLength  the maximum length of the decoded frame.
     *                   A {@link TooLongFrameException} is thrown if
     *                   the length of the frame exceeds this value.
     */
    public LineBasedFrameDecoder(final int maxLength) {
        this(maxLength, true, false);
    }

    /**
     * Creates a new decoder.
     * @param maxLength  the maximum length of the decoded frame.
     *                   A {@link TooLongFrameException} is thrown if
     *                   the length of the frame exceeds this value.
     * @param stripDelimiter  whether the decoded frame should strip out the
     *                        delimiter or not
     * @param failFast  If <tt>true</tt>, a {@link TooLongFrameException} is
     *                  thrown as soon as the decoder notices the length of the
     *                  frame will exceed <tt>maxFrameLength</tt> regardless of
     *                  whether the entire frame has been read.
     *                  If <tt>false</tt>, a {@link TooLongFrameException} is
     *                  thrown after the entire frame that exceeds
     *                  <tt>maxFrameLength</tt> has been read.
     */
    public LineBasedFrameDecoder(final int maxLength, final boolean stripDelimiter, final boolean failFast) {
        this.maxLength = maxLength;
        this.failFast = failFast;
        this.stripDelimiter = stripDelimiter;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = decode(ctx, in);
        // 如果解析出来了对象，只要将对象放到List里面，父类会循环向下传播Handler
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param   ctx             the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param   buffer          the {@link ByteBuf} from which to read data
     * @return  frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     *                          be created.
     */
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
    	// 获取第一个换行符的位置（注意：如果换行符连续在一起，默认当成一个，且取第一个）
        final int eol = findEndOfLine(buffer);
        // 是否丢弃数据（首次是false）
        if (!discarding) {
            if (eol >= 0) {
                final ByteBuf frame;
                // 获取一行数据的长度
                final int length = eol - buffer.readerIndex();
                // 获取到分割符的长度
                final int delimLength = buffer.getByte(eol) == '\r'? 2 : 1;
                // 一行数据超过了最大限制
                if (length > maxLength) {
                	// 设置数据已读取到的位置（就是最后一个分割符的位置）
                    buffer.readerIndex(eol + delimLength);
                    // 传播异常
                    fail(ctx, length);
                    return null;
                }
                // 是否不需要携带分割符
                if (stripDelimiter) {
                	// 截取固定长度的数据（从读取位置开始截），生成新的ByteBuf
                    frame = buffer.readRetainedSlice(length);
                    // 跳过分割符字节长度（就是将数据已读取到的位置往后移几位）
                    buffer.skipBytes(delimLength);
                } else {
                	// 截取固定长度的数据（从读取位置开始截），生成新的ByteBuf
                    frame = buffer.readRetainedSlice(length + delimLength);
                }
                return frame;
            // 如果没有找到换行符    
            } else {
            	// 获取数据可读长度
                final int length = buffer.readableBytes();
                // 一行数据超过了最大限制
                if (length > maxLength) {
                    discardedBytes = length;
                    // 将可读数据位置，标记为写写指针
                    buffer.readerIndex(buffer.writerIndex());
                    // 丢弃为true
                    discarding = true;
                    // 抛异常
                    offset = 0;
                    if (failFast) {
                        fail(ctx, "over " + discardedBytes);
                    }
                }
                return null;
            }
        // 丢弃    
        } else {
            if (eol >= 0) {
                final int length = discardedBytes + eol - buffer.readerIndex();
                final int delimLength = buffer.getByte(eol) == '\r'? 2 : 1;
                buffer.readerIndex(eol + delimLength);
                discardedBytes = 0;
                discarding = false;
                if (!failFast) {
                    fail(ctx, length);
                }
            } else {
                discardedBytes += buffer.readableBytes();
                buffer.readerIndex(buffer.writerIndex());
                // We skip everything in the buffer, we need to set the offset to 0 again.
                offset = 0;
            }
            return null;
        }
    }

    private void fail(final ChannelHandlerContext ctx, int length) {
        fail(ctx, String.valueOf(length));
    }

    private void fail(final ChannelHandlerContext ctx, String length) {
        ctx.fireExceptionCaught(
                new TooLongFrameException(
                        "frame length (" + length + ") exceeds the allowed maximum (" + maxLength + ')'));
    }

    /**
     * 获取第一个换行符的位置（注意：如果换行符连续在一起，默认当成一个，且取第一个）
     * Returns the index in the buffer of the end of line found.
     * Returns -1 if no end of line was found in the buffer.
     */
    private int findEndOfLine(final ByteBuf buffer) {
        int totalLength = buffer.readableBytes();
        int i = buffer.forEachByte(buffer.readerIndex() + offset, totalLength - offset, ByteProcessor.FIND_LF);
        if (i >= 0) {
            offset = 0;
            if (i > 0 && buffer.getByte(i - 1) == '\r') {
                i--;
            }
        } else {
            offset = totalLength;
        }
        return i;
    }
}
