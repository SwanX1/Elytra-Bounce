/**
 * Copyright 2022 Infernal Studios
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infernalstudios.elytrabounce.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> entity, World world) {
		super(entity, world);
	}

	@Unique private int ticksOnGround = 0;
	@Unique private boolean wasGoodBefore = false;

	@Unique private static final int MAX_GROUNDED_TIME = 5;

	@ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V"), index = 1)
	private boolean updateTravel(boolean isOnGround) {
		if (ticksOnGround <= MAX_GROUNDED_TIME) return true;
		return isOnGround;
	}

	@Inject(method = "tickFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void updateFallFlying(CallbackInfo ci, boolean flag) {
		if (wasGoodBefore && !flag && this.isOnGround()) {
			ticksOnGround++;
			wasGoodBefore = true;
			this.setFlag(7, true);
			return;
		}

		ticksOnGround = 0;
		wasGoodBefore = flag;
	}

}
