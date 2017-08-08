package net.blay09.mods.hardcorerevival.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageRevival implements IMessage {
	private boolean active;

	public MessageRevival() {
	}

	public MessageRevival(boolean active) {
		this.active = active;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		active = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(active);
	}

	public boolean isActive() {
		return active;
	}
}
