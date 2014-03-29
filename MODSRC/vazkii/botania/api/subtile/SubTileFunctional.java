/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jan 24, 2014, 8:03:44 PM (GMT)]
 */
package vazkii.botania.api.subtile;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaPool;

/**
 * The basic class for a Functional Flower.
 */
public class SubTileFunctional extends SubTileEntity {

	private static final String TAG_MANA = "mana";

	private static final String TAG_POOL_X = "poolX";
	private static final String TAG_POOL_Y = "poolY";
	private static final String TAG_POOL_Z = "poolZ";

	public int mana;

	int sizeLastCheck = -1;
	TileEntity linkedPool = null;
	public int knownMana = -1;

	ChunkCoordinates cachedPoolCoordinates = null;

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		linkPool();

		if(linkedPool != null) {
			IManaPool pool = (IManaPool) linkedPool;
			int manaInPool = pool.getCurrentMana();
			int manaMissing = getMaxMana() - mana;
			int manaToRemove = Math.min(manaMissing, manaInPool);
			pool.recieveMana(-manaToRemove);
			addMana(manaToRemove);
		}

		if(supertile.getWorldObj().isRemote) {
			double particleChance = 1F - (double) mana / (double) getMaxMana() / 2F;
			Color color = new Color(getColor());
			if(Math.random() > particleChance)
				BotaniaAPI.internalHandler.sparkleFX(supertile.getWorldObj(), supertile.xCoord + 0.3 + Math.random() * 0.5, supertile.yCoord + 0.5 + Math.random()  * 0.5, supertile.zCoord + 0.3 + Math.random() * 0.5, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (float) Math.random(), 5);
		}
	}

	public void linkPool() {
		final int range = 6;

		boolean needsNew = false;
		if(linkedPool == null) {
			needsNew = true;

			if(cachedPoolCoordinates != null) {
				TileEntity tileAt = supertile.getWorldObj().getTileEntity(cachedPoolCoordinates.posX, cachedPoolCoordinates.posY, cachedPoolCoordinates.posZ);
				if(tileAt != null && tileAt instanceof IManaPool) {
					linkedPool = tileAt;
					needsNew = false;
				}
				cachedPoolCoordinates = null;
			}
		}

		if(!needsNew) {
			TileEntity tileAt = supertile.getWorldObj().getTileEntity(linkedPool.xCoord, linkedPool.yCoord, linkedPool.zCoord);
			if(!(tileAt instanceof IManaPool)) {
				linkedPool = null;
				needsNew = true;
			} else linkedPool = tileAt;
		}

		if(needsNew) {
			IManaNetwork network = BotaniaAPI.internalHandler.getManaNetworkInstance();
			int size = network.getAllPoolsInWorld(supertile.getWorldObj().provider.dimensionId).size();
			if(size != sizeLastCheck) {
				ChunkCoordinates coords = new ChunkCoordinates(supertile.xCoord, supertile.yCoord, supertile.zCoord);
				linkedPool = network.getClosestPool(coords, supertile.getWorldObj().provider.dimensionId, range);
				sizeLastCheck = size;
			}
		}
	}

	public void addMana(int mana) {
		this.mana = Math.min(getMaxMana(), this.mana + mana);
	}

	@Override
	public boolean onWanded(EntityPlayer player, ItemStack wand) {
		knownMana = mana;
		player.worldObj.playSoundAtEntity(player, "random.orb", 0.1F, 1F);

		return super.onWanded(player, wand);
	}

	public int getMaxMana() {
		return 20;
	}

	public int getColor() {
		return 0xFFFFFF;
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		mana = cmp.getInteger(TAG_MANA);

		int x = cmp.getInteger(TAG_POOL_X);
		int y = cmp.getInteger(TAG_POOL_Y);
		int z = cmp.getInteger(TAG_POOL_Z);

		cachedPoolCoordinates = new ChunkCoordinates(x, y, z);
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp) {
		cmp.setInteger(TAG_MANA, mana);

		int x = linkedPool == null ? 0 : linkedPool.xCoord;
		int y = linkedPool == null ? -1 : linkedPool.yCoord;
		int z = linkedPool == null ? 0 : linkedPool.zCoord;

		cmp.setInteger(TAG_POOL_X, x);
		cmp.setInteger(TAG_POOL_Y, y);
		cmp.setInteger(TAG_POOL_Z, z);
	}

	@Override
	public ChunkCoordinates getBinding() {
		if(linkedPool == null)
			return null;
		return new ChunkCoordinates(linkedPool.xCoord, linkedPool.yCoord, linkedPool.zCoord);
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res) {
		String name = StatCollector.translateToLocal("tile.botania:flower." + getUnlocalizedName() + ".name");
		int color = 0x66000000 | getColor();
		BotaniaAPI.internalHandler.drawSimpleManaHUD(color, knownMana, getMaxMana(), name, res);
	}

}
