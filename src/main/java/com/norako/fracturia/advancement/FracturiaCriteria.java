package com.norako.fracturia.advancement;

import com.norako.fracturia.Fracturia;
import net.minecraft.advancement.criterion.Criteria;

public class FracturiaCriteria {

    public static final SurvivedVoidedCriterion SURVIVED_VOIDED =
            Criteria.register(Fracturia.MOD_ID + ":survived_voided", new SurvivedVoidedCriterion());

    public static final EnteredPortalRoomCriterion ENTERED_PORTAL_ROOM =
            Criteria.register(Fracturia.MOD_ID + ":entered_portal_room", new EnteredPortalRoomCriterion());

    public static final KilledEndersentVariantCriterion KILLED_ENDERSENT_VARIANT =
            Criteria.register(Fracturia.MOD_ID + ":killed_endersent_variant", new KilledEndersentVariantCriterion());

    public static final KilledAllEndersentCriterion KILLED_ALL_ENDERSENT =
            Criteria.register(Fracturia.MOD_ID + ":killed_all_endersent", new KilledAllEndersentCriterion());

    public static void register() {
        Fracturia.LOGGER.info("Registering Fracturia Criteria for " + Fracturia.MOD_ID);
    }
}
