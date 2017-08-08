package net.blay09.mods.hardcorerevival.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageRevivalProgress implements IMessage {
	private int entityId;
	private float progress;

	public MessageRevivalProgress() {
	}

	public MessageRevivalProgress(int entityId, float progress) {
		this.entityId = entityId;
		this.progress = progress;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
		progress = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeFloat(progress);
	}

	public int getEntityId() {
		return entityId;
	}

	public float getProgress() {
		return progress;
	}
}
