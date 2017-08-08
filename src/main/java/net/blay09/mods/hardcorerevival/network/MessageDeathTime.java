package net.blay09.mods.hardcorerevival.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageDeathTime implements IMessage {
	private int deathTime;

	public MessageDeathTime() {
	}

	public MessageDeathTime(int deathTime) {
		this.deathTime = deathTime;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		deathTime = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(deathTime);
	}

	public int getDeathTime() {
		return deathTime;
	}
}
