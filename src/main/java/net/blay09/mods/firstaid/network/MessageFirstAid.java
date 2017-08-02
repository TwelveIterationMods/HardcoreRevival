package net.blay09.mods.firstaid.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageFirstAid implements IMessage {
	private boolean active;

	public MessageFirstAid() {
	}

	public MessageFirstAid(boolean active) {
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
