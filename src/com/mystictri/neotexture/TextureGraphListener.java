package com.mystictri.neotexture;

import engine.graphics.synthesis.texture.Channel;

public interface TextureGraphListener {
	
	public void nodeDeleted(TextureGraphNode node);
	
	/**
	 * This function is used to notify when a single channel in a node changed
	 * @param channel the channel that changed
	 */
	public void channelInNodeChanged(Channel source);
}
