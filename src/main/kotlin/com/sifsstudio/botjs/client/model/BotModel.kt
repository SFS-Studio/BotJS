package com.sifsstudio.botjs.client.model

import com.sifsstudio.botjs.client.renderer.entity.state.BotEntityRendererState
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class BotModel(pRoot: ModelPart) : HumanoidModel<BotEntityRendererState>(pRoot)