package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;
import pneumaticCraft.proxy.CommonProxy;

public class BlockPneumaticDynamo extends BlockPneumaticCraftModeled{

    protected BlockPneumaticDynamo(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPneumaticDynamo.class;
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_PNEUMATIC_DYNAMO;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

}
