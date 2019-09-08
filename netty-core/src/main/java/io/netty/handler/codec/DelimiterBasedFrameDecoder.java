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

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 基于分割符的解码器
 * A decoder that splits the received {@link ByteBuf}s by one or more
 * delimiters.  It is particularly useful for decoding the frames which ends
 * with a delimiter such as {@link Delimiters#nulDelimiter() NUL} or
 * {@linkplain Delimiters#lineDelimiter() newline characters}.
 *
 * <h3>Predefined delimiters</h3>
 * <p>
 * {@link Delimiters} defines frequently used delimiters for convenience' sake.
 *
 * <h3>Specifying more than one delimiter</h3>
 * <p>
 * {@link DelimiterBasedFrameDecoder} allows you to specify more than one
 * delimiter.  If more than one delimiter is found in the buffer, it chooses
 * the delimiter which produces the shortest frame.  For example, if you have
 * the following data in the buffer:
 * 客户端传过来的数据如下:
 * <pre>
 * +--------------+
 * | ABC\nDEF\r\n |
 * +--------------+
 * </pre>
 * a {@link DelimiterBasedFrameDecoder}({@link Delimiters#lineDelimiter() Delimiters.lineDelimiter()})
 * will choose {@code '\n'} as the first delimiter and produce two frames:
 * <pre>
 * +-----+-----+
 * | ABC | DEF |
 * +-----+-----+
 * </pre>
 * rather than incorrectly choosing {@code '\r\n'} as the first delimiter:
 * <pre>
 * +----------+
 * | ABC\nDEF |
 * +----------+
 * </pre>
 */
public class DelimiterBasedFrameDecoder extends ByteToMessageDecoder {

    private final ByteBuf[] delimiters;
    // 分割后单条数据最大长度
    private final int maxFrameLength;
    // 分割出来的数据是否不携带分割符
    private final boolean stripDelimiter;
    // 分割后单条数据超过最大长度是否抛出异常
    private final boolean failFast;
    // 是否需要丢弃数据
    private boolean discardingTooLongFrame;
    // 当前丢弃的字节数
    private int tooLongFrameLength;
    // 换行符解码器
    /** Set only when decoding with "\n" and "\r\n" as the delimiter.  */
    private final LineBasedFrameDecoder lineBasedDecoder;

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength  the maximum length of the decoded frame.
     *                        A {@link TooLongFrameException} is thrown if
     *                        the length of the frame exceeds this value.
     * @param delimiter  the delimiter
     */
    public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter) {
        this(maxFrameLength, true, delimiter);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength  the maximum length of the decoded frame.
     *                        A {@link TooLongFrameException} is thrown if
     *                        the length of the frame exceeds this value.
     * @param stripDelimiter  whether the decoded frame should strip out the
     *                        delimiter or not
     * @param delimiter  the delimiter
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter) {
        this(maxFrameLength, stripDelimiter, true, delimiter);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength  the maximum length of the decoded frame.
     *                        A {@link TooLongFrameException} is thrown if
     *                        the length of the frame exceeds this value.
     * @param stripDelimiter  whether the decoded frame should strip out the
     *                        delimiter or not
     * @param failFast  If <tt>true</tt>, a {@link TooLongFrameException} is
     *                  thrown as soon as the decoder notices the length of the
     *                  frame will exceed <tt>maxFrameLength</tt> regardless of
     *                  whether the entire frame has been read.
     *                  If <tt>false</tt>, a {@link TooLongFrameException} is
     *                  thrown after the entire frame that exceeds
     *                  <tt>maxFrameLength</tt> has been read.
     * @param delimiter  the delimiter
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, boolean failFast,
            ByteBuf delimiter) {
        this(maxFrameLength, stripDelimiter, failFast, new ByteBuf[] {
                delimiter.slice(delimiter.readerIndex(), delimiter.readableBytes())});
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength  the maximum length of the decoded frame.（分割后单条数据最大长度）
     *                        A {@link TooLongFrameException} is thrown if
     *                        the length of the frame exceeds this value.
     * @param delimiters  the delimiters 分割符
     */
    public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters) {
        this(maxFrameLength, true, delimiters);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength  the maximum length of the decoded frame.
     *                        A {@link TooLongFrameException} is thrown if
     *                        the length of the frame exceeds this value.
     * @param stripDelimiter  whether the decoded frame should strip out the
     *                        delimiter or not
     * @param delimiters  the delimiters
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, ByteBuf... delimiters) {
        this(maxFrameLength, stripDelimiter, true, delimiters);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength  the maximum length of the decoded frame.
     *                        A {@link TooLongFrameException} is thrown if
     *                        the length of the frame exceeds this value.
     * @param stripDelimiter  whether the decoded frame should strip out the
     *                        delimiter or not
     * @param failFast  If <tt>true</tt>, a {@link TooLongFrameException} is
     *                  thrown as soon as the decoder notices the length of the
     *                  frame will exceed <tt>maxFrameLength</tt> regardless of
     *                  whether the entire frame has been read.
     *                  If <tt>false</tt>, a {@link TooLongFrameException} is
     *                  thrown after the entire frame that exceeds
     *                  <tt>maxFrameLength</tt> has been read.
     * @param delimiters  the delimiters
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf... delimiters) {
        validateMaxFrameLength(maxFrameLength);
        if (delimiters == null) {
            throw new NullPointerException("delimiters");
        }
        if (delimiters.length == 0) {
            throw new IllegalArgumentException("empty delimiters");
        }

        // 如果分割符是换行符，直接初始化换行符解码器
        if (isLineBased(delimiters) && !isSubclass()) {
            lineBasedDecoder = new LineBasedFrameDecoder(maxFrameLength, stripDelimiter, failFast);
            this.delimiters = null;
        } else {
        	// 所有分割符
            this.delimiters = new ByteBuf[delimiters.length];
            for (int i = 0; i < delimiters.length; i ++) {
            	// 分割符
                ByteBuf d = delimiters[i];
                // 验证分割符是否null
                validateDelimiter(d);
                // 获取分割符数据
                this.delimiters[i] = d.slice(d.readerIndex(), d.readableBytes());
            }
            lineBasedDecoder = null;
        }
        this.maxFrameLength = maxFrameLength;
        this.stripDelimiter = stripDelimiter;
        this.failFast = failFast;
    }

    /**
     * 判断分割符是否是换行符
     */
    /** Returns true if the delimiters are "\n" and "\r\n".  */
    private static boolean isLineBased(final ByteBuf[] delimiters) {
        if (delimiters.length != 2) {
            return false;
        }
        ByteBuf a = delimiters[0];
        ByteBuf b = delimiters[1];
        if (a.capacity() < b.capacity()) {
            a = delimiters[1];
            b = delimiters[0];
        }
        return a.capacity() == 2 && b.capacity() == 1
                && a.getByte(0) == '\r' && a.getByte(1) == '\n'
                && b.getByte(0) == '\n';
    }

    /**
     * Return {@code true} if the current instance is a subclass of DelimiterBasedFrameDecoder
     */
    private boolean isSubclass() {
        return getClass() != DelimiterBasedFrameDecoder.class;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    	// 如果解析出来了对象，只要将对象放到List里面，父类会循环向下传播Handler
        Object decoded = decode(ctx, in);
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
    	// 如果换行符解码器不为空，就直接使用换行符解码器
        if (lineBasedDecoder != null) {
            return lineBasedDecoder.decode(ctx, buffer);
        }
        // Try all delimiters and choose the delimiter which yields the shortest frame.
        int minFrameLength = Integer.MAX_VALUE;
        ByteBuf minDelim = null;
        // 循环迭代所有的分割符，分割数据，找到最小数据的长度
        // 注意：其实就是找到第一个分割符分割出来的数据的长度（为什么要这样找，是因为不知道那个分割符会在前面）
        for (ByteBuf delim: delimiters) {
            int frameLength = indexOf(buffer, delim);
            if (frameLength >= 0 && frameLength < minFrameLength) {
            	// 第一个分割符分割出来的数据的长度
                minFrameLength = frameLength;
                // 分割符
                minDelim = delim;
            }
        }
        // 找到了第一个分割符
        if (minDelim != null) {
        	// 第一个分割符的长度
            int minDelimLength = minDelim.capacity();
            ByteBuf frame;
            // 是否需要丢弃数据（首次是false）
            if (discardingTooLongFrame) {
            	// 标记不丢弃
                // We've just finished discarding a very large frame.
                // Go back to the initial state.
                discardingTooLongFrame = false;
                // 跳过第一个分割符分割出来的数据的长度加分割符字节长度（就是将数据已读取到的位置往后移几位）
                buffer.skipBytes(minFrameLength + minDelimLength);

                int tooLongFrameLength = this.tooLongFrameLength;
                this.tooLongFrameLength = 0;
                if (!failFast) {
                    fail(tooLongFrameLength);
                }
                return null;
            }
            // 第一个分割符分割出来的数据的长度 大于 最大长度的限制
            if (minFrameLength > maxFrameLength) {
            	// 跳过第一个分割符分割出来的数据的长度加分割符字节长度（就是将数据已读取到的位置往后移几位）
                // Discard read frame.
                buffer.skipBytes(minFrameLength + minDelimLength);
                // 抛异常
                fail(minFrameLength);
                return null;
            }
            
            // 分割出来的数据不携带分割符
            if (stripDelimiter) {
            	// 截取固定长度的数据（从读取位置开始截），生成新的ByteBuf
                frame = buffer.readRetainedSlice(minFrameLength);
                // 跳过分割符字节长度（就是将数据已读取到的位置往后移几位）
                buffer.skipBytes(minDelimLength);
            } else {
            	// 截取固定长度的数据（从读取位置开始截），生成新的ByteBuf
                frame = buffer.readRetainedSlice(minFrameLength + minDelimLength);
            }

            return frame;
            
        // 没有找到第一个分割符    
        } else {
            if (!discardingTooLongFrame) {
                if (buffer.readableBytes() > maxFrameLength) {
                    // Discard the content of the buffer until a delimiter is found.
                    tooLongFrameLength = buffer.readableBytes();
                    buffer.skipBytes(buffer.readableBytes());
                    discardingTooLongFrame = true;
                    if (failFast) {
                        fail(tooLongFrameLength);
                    }
                }
            } else {
                // Still discarding the buffer since a delimiter is not found.
                tooLongFrameLength += buffer.readableBytes();
                buffer.skipBytes(buffer.readableBytes());
            }
            return null;
        }
    }

    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException(
                            "frame length exceeds " + maxFrameLength +
                            ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException(
                            "frame length exceeds " + maxFrameLength +
                            " - discarding");
        }
    }

    /**
     * Returns the number of bytes between the readerIndex of the haystack and
     * the first needle found in the haystack.  -1 is returned if no needle is
     * found in the haystack.
     */
    private static int indexOf(ByteBuf haystack, ByteBuf needle) {
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i ++) {
            int haystackIndex = i;
            int needleIndex;
            for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex ++) {
                if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
                    break;
                } else {
                    haystackIndex ++;
                    if (haystackIndex == haystack.writerIndex() &&
                        needleIndex != needle.capacity() - 1) {
                        return -1;
                    }
                }
            }

            if (needleIndex == needle.capacity()) {
                // Found the needle from the haystack!
                return i - haystack.readerIndex();
            }
        }
        return -1;
    }

    private static void validateDelimiter(ByteBuf delimiter) {
        if (delimiter == null) {
            throw new NullPointerException("delimiter");
        }
        if (!delimiter.isReadable()) {
            throw new IllegalArgumentException("empty delimiter");
        }
    }

    private static void validateMaxFrameLength(int maxFrameLength) {
        checkPositive(maxFrameLength, "maxFrameLength");
    }
}
