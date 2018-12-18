package com.syj.tcpentrypoint.transport;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 *
 * Allocator Example: PooledBufHolder.getBuffer()
 *
 */
public class PooledBufHolder {

    private static ByteBufAllocator pooled = new UnpooledByteBufAllocator(false);

    public static ByteBufAllocator getInstance(){

        return pooled;
    }

    public static ByteBuf getBuffer(){

        return pooled.buffer();
    }

    public static ByteBuf getBuffer(int size){
        return pooled.buffer(size);
    }

}