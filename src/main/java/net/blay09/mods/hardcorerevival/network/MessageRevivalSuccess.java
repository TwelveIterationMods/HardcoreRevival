package net.blay09.mods.hardcorerevival.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageRevivalSuccess implements IMessage {
	private int entityId;

	public MessageRevivalSuccess() {
	}

	public MessageRevivalSuccess(int entityId) {
		this.entityId = entityId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
	}

	public int getEntityId() {
		return entityId;
	}
}
