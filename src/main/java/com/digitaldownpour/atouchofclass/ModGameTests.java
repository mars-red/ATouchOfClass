package com.digitaldownpour.atouchofclass;

import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers test functions and their structure-backed instances for Minecraft 1.21.9. */
public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, ATouchofClass.MODID);

    static {
        FUNCTIONS.register("poplarfurnituregametests.railingfamilycraftsunlocksanddrops", () -> PoplarFurnitureGameTests::railingFamilyCraftsUnlocksAndDrops);
        FUNCTIONS.register("poplarfurnituregametests.talldoorcraftsplacesandpreserveskeys", () -> PoplarFurnitureGameTests::tallDoorCraftsPlacesAndPreservesKeys);
        FUNCTIONS.register("poplarfurnituregametests.tallsidelightcraftsplacesanddrops", () -> PoplarFurnitureGameTests::tallSidelightCraftsPlacesAndDrops);
        FUNCTIONS.register("poplarfurnituregametests.everybedcolorcraftsunlocksanddrops", () -> PoplarFurnitureGameTests::everyBedColorCraftsUnlocksAndDrops);
        FUNCTIONS.register("poplarfurnituregametests.parquetcraftsunlocksanddrops", () -> PoplarFurnitureGameTests::parquetCraftsUnlocksAndDrops);
        FUNCTIONS.register("poplarfurnituregametests.diningtableandchaircraftanddrop", () -> PoplarFurnitureGameTests::diningTableAndChairCraftAndDrop);
        FUNCTIONS.register("poplarfurnituregametests.tablesnightstandsanddresserscraftanddrop", () -> PoplarFurnitureGameTests::tablesNightstandsAndDressersCraftAndDrop);
        FUNCTIONS.register("cabinetgametests.poplarcabinetscraftdropandstack", () -> CabinetGameTests::poplarCabinetsCraftDropAndStack);
        FUNCTIONS.register("concretefurnituregametests.everycolorhasslabrecipesandunlocks", () -> ConcreteFurnitureGameTests::everyColorHasSlabRecipesAndUnlocks);
        FUNCTIONS.register("baseboardgametests.uprightplinthscreatebaseboardcorners", () -> BaseboardGameTests::uprightPlinthsCreateBaseboardCorners);
        FUNCTIONS.register("baseboardgametests.apronswithbaseboardcreatecorners", () -> BaseboardGameTests::apronsWithBaseboardCreateCorners);
        FUNCTIONS.register("baseboardgametests.unrelatedtrimdoesnotcreatecorners", () -> BaseboardGameTests::unrelatedTrimDoesNotCreateCorners);
        FUNCTIONS.register("cabinetgametests.everycabinetvariantprovidespersistentcheststorage", () -> CabinetGameTests::everyCabinetVariantProvidesPersistentChestStorage);
        FUNCTIONS.register("cabinetgametests.matchingdoorcabinetsjoinwhenstacked", () -> CabinetGameTests::matchingDoorCabinetsJoinWhenStacked);
        FUNCTIONS.register("chairgametests.chairseatsplayerandcleansupseatwhenbroken", () -> ChairGameTests::chairSeatsPlayerAndCleansUpSeatWhenBroken);
        FUNCTIONS.register("chandeliergametests.ceilingandchainsupport", () -> ChandelierGameTests::ceilingAndChainSupport);
        FUNCTIONS.register("chandeliergametests.oxidationwaxingandlight", () -> ChandelierGameTests::oxidationWaxingAndLight);
        FUNCTIONS.register("chandeliergametests.chainstatesurvivestransitions", () -> ChandelierGameTests::chainStateSurvivesTransitions);
        FUNCTIONS.register("copperfurnituregametests.countertopsoxidizewaxandusevanillacopperproperties", () -> CopperFurnitureGameTests::countertopsOxidizeWaxAndUseVanillaCopperProperties);
        FUNCTIONS.register("copperfurnituregametests.sinkspreservefacingthroughcoppertransitions", () -> CopperFurnitureGameTests::sinksPreserveFacingThroughCopperTransitions);
        FUNCTIONS.register("copperfurnituregametests.lamppostspreservetheirsectionthroughcoppertransitions", () -> CopperFurnitureGameTests::lampPostsPreserveTheirSectionThroughCopperTransitions);
        FUNCTIONS.register("copperfurnituregametests.floorlampspreservetheirsectionthroughcoppertransitions", () -> CopperFurnitureGameTests::floorLampsPreserveTheirSectionThroughCopperTransitions);
        FUNCTIONS.register("copperfurnituregametests.ceilinglightfamiliesoxidizeandwax", () -> CopperFurnitureGameTests::ceilingLightFamiliesOxidizeAndWax);
        FUNCTIONS.register("copperfurnituregametests.tablelampsoxidizeandwax", () -> CopperFurnitureGameTests::tableLampsOxidizeAndWax);
        FUNCTIONS.register("copperfurnituregametests.sconceschainsandchandeliersusevanillacopperproperties", () -> CopperFurnitureGameTests::sconcesChainsAndChandeliersUseVanillaCopperProperties);
        FUNCTIONS.register("diningchairgametests.everywoodseatsfacesanddismountsineverydirection", () -> DiningChairGameTests::everyWoodSeatsFacesAndDismountsInEveryDirection);
        FUNCTIONS.register("diningchairgametests.everywoodhascorrectrecipeitemmaterialanddrop", () -> DiningChairGameTests::everyWoodHasCorrectRecipeItemMaterialAndDrop);
        FUNCTIONS.register("diningchairgametests.dismountstepsclearofchairineveryfacing", () -> DiningChairGameTests::dismountStepsClearOfChairInEveryFacing);
        FUNCTIONS.register("diningchairgametests.dismountavoidstableandblockedside", () -> DiningChairGameTests::dismountAvoidsTableAndBlockedSide);
        FUNCTIONS.register("diningchairgametests.blockedexitsdismountaboveseatinsteadofinsidechair", () -> DiningChairGameTests::blockedExitsDismountAboveSeatInsteadOfInsideChair);
        FUNCTIONS.register("diningchairgametests.seatingfacesfrontineverydirectionandcleansup", () -> DiningChairGameTests::seatingFacesFrontInEveryDirectionAndCleansUp);
        FUNCTIONS.register("diningchairgametests.actualplacementfacesplayerandhasmatchingtallback", () -> DiningChairGameTests::actualPlacementFacesPlayerAndHasMatchingTallBack);
        FUNCTIONS.register("diningchairgametests.supportlossdismountsanddropsonechair", () -> DiningChairGameTests::supportLossDismountsAndDropsOneChair);
        FUNCTIONS.register("diningchairgametests.stickandoakslabrecipemakestwochairs", () -> DiningChairGameTests::stickAndOakSlabRecipeMakesTwoChairs);
        FUNCTIONS.register("diningtablegametests.rectanglehasfourlegsandcontinuoustop", () -> DiningTableGameTests::rectangleHasFourLegsAndContinuousTop);
        FUNCTIONS.register("diningtablegametests.diagonalremovalcreatesandrefillsinsidecorner", () -> DiningTableGameTests::diagonalRemovalCreatesAndRefillsInsideCorner);
        FUNCTIONS.register("diningtablegametests.isolatedmatchessidetableanddoesnotjoinotherfurniture", () -> DiningTableGameTests::isolatedMatchesSideTableAndDoesNotJoinOtherFurniture);
        FUNCTIONS.register("diningtablegametests.holehasfourinsidecornersandtransformspreservestate", () -> DiningTableGameTests::holeHasFourInsideCornersAndTransformsPreserveState);
        FUNCTIONS.register("diningtablegametests.everywoodformsfourlegrectangle", () -> DiningTableGameTests::everyWoodFormsFourLegRectangle);
        FUNCTIONS.register("diningtablegametests.onlymatchingwoodpairsconnect", () -> DiningTableGameTests::onlyMatchingWoodPairsConnect);
        FUNCTIONS.register("diningtablegametests.differentwooddiagonaldoesnotfillinsidecorner", () -> DiningTableGameTests::differentWoodDiagonalDoesNotFillInsideCorner);
        FUNCTIONS.register("dressergametests.everyvariantusesthesharedsixrowinventory", () -> DresserGameTests::everyVariantUsesTheSharedSixRowInventory);
        FUNCTIONS.register("dressergametests.dresserbuildsandbreaksasonepersistentfourpartblock", () -> DresserGameTests::dresserBuildsAndBreaksAsOnePersistentFourPartBlock);
        FUNCTIONS.register("fourpostbedgametests.everywoodandbeddingcolorsleepsabovemattress", () -> FourPostBedGameTests::everyWoodAndBeddingColorSleepsAboveMattress);
        FUNCTIONS.register("fourpostbedgametests.bothmattresssideskeepcorrectheightineveryfacing", () -> FourPostBedGameTests::bothMattressSidesKeepCorrectHeightInEveryFacing);
        FUNCTIONS.register("fourpostbedgametests.vanillabedskeepvanillasleepingheight", () -> FourPostBedGameTests::vanillaBedsKeepVanillaSleepingHeight);
        FUNCTIONS.register("inductioncooktopgametests.cooktopsjoininpairsandrejectathirdconnection", () -> InductionCooktopGameTests::cooktopsJoinInPairsAndRejectAThirdConnection);
        FUNCTIONS.register("keyringgametests.survivalbreakandpickupcodeddoor", () -> KeyRingGameTests::survivalBreakAndPickupCodedDoor);
        FUNCTIONS.register("keyringgametests.survivalbreakandpickuppreviouslysavedcodeddoor", () -> KeyRingGameTests::survivalBreakAndPickupPreviouslySavedCodedDoor);
        FUNCTIONS.register("keyringgametests.survivalbreakandpickupafterusingcodeddoor", () -> KeyRingGameTests::survivalBreakAndPickupAfterUsingCodedDoor);
        FUNCTIONS.register("keyringgametests.anvilrenamingpreserveskeys", () -> KeyRingGameTests::anvilRenamingPreservesKeys);
        FUNCTIONS.register("keyringgametests.texturestatetracksfirstandlastkey", () -> KeyRingGameTests::textureStateTracksFirstAndLastKey);
        FUNCTIONS.register("keyringgametests.transferscapacityandpersistence", () -> KeyRingGameTests::transfersCapacityAndPersistence);
        FUNCTIONS.register("keyringgametests.openringcannotmove", () -> KeyRingGameTests::openRingCannotMove);
        FUNCTIONS.register("keyringgametests.ringandindividualkeysoperatedoors", () -> KeyRingGameTests::ringAndIndividualKeysOperateDoors);
        FUNCTIONS.register("keyringgametests.placingandbreakingcodeddooritempreserveskeycode", () -> KeyRingGameTests::placingAndBreakingCodedDoorItemPreservesKeyCode);
        FUNCTIONS.register("keyringgametests.breakingcodeddoorpreserveskeycode", () -> KeyRingGameTests::breakingCodedDoorPreservesKeyCode);
        FUNCTIONS.register("kitchensinkgametests.pairineitherorderandsplitineveryfacing", () -> KitchenSinkGameTests::pairInEitherOrderAndSplitInEveryFacing);
        FUNCTIONS.register("kitchensinkgametests.rowsstopatpairsandbridgechoosesonlyoneneighbor", () -> KitchenSinkGameTests::rowsStopAtPairsAndBridgeChoosesOnlyOneNeighbor);
        FUNCTIONS.register("kitchensinkgametests.mismatchedneighborsandrotatedpartnersdonotjoin", () -> KitchenSinkGameTests::mismatchedNeighborsAndRotatedPartnersDoNotJoin);
        FUNCTIONS.register("kitchensinkgametests.shapesandstructuretransformskeeptheaprondecorative", () -> KitchenSinkGameTests::shapesAndStructureTransformsKeepTheApronDecorative);
        FUNCTIONS.register("kitchensinkgametests.recipeandeachhalfdroponesink", () -> KitchenSinkGameTests::recipeAndEachHalfDropOneSink);
        FUNCTIONS.register("leatherseatinggametests.everyleatherseatfacesfrontanddismountsontofloor", () -> LeatherSeatingGameTests::everyLeatherSeatFacesFrontAndDismountsOntoFloor);
        FUNCTIONS.register("leatherseatinggametests.leatherseatsfindanotherexitwhenfrontisblocked", () -> LeatherSeatingGameTests::leatherSeatsFindAnotherExitWhenFrontIsBlocked);
        FUNCTIONS.register("leatherseatinggametests.surroundedleatherseatsdismountabovefurniture", () -> LeatherSeatingGameTests::surroundedLeatherSeatsDismountAboveFurniture);
        FUNCTIONS.register("lockeddoorgametests.rejecteddoorinteractiondoesnotplaceheldblock", () -> LockedDoorGameTests::rejectedDoorInteractionDoesNotPlaceHeldBlock);
        FUNCTIONS.register("loveseatgametests.loveseatplacesbreaksandsupportsindependentseats", () -> LoveSeatGameTests::loveSeatPlacesBreaksAndSupportsIndependentSeats);
        FUNCTIONS.register("newelpostgametests.everywoodnewelplacesandremovesbothhalves", () -> NewelPostGameTests::everyWoodNewelPlacesAndRemovesBothHalves);
        FUNCTIONS.register("newelpostgametests.quadrantsandtwoblocklifecycle", () -> NewelPostGameTests::quadrantsAndTwoBlockLifecycle);
        FUNCTIONS.register("nightstandgametests.everynightstandisfullheightandprovidespersistentcheststorage", () -> NightstandGameTests::everyNightstandIsFullHeightAndProvidesPersistentChestStorage);
        FUNCTIONS.register("oakbalustradegametests.bottomlandingsblendandreset", () -> OakBalustradeGameTests::bottomLandingsBlendAndReset);
        FUNCTIONS.register("oakbalustradegametests.toptransitionsfollowanylandingneighbor", () -> OakBalustradeGameTests::topTransitionsFollowAnyLandingNeighbor);
        FUNCTIONS.register("oakbalustradegametests.diagonalrunsplaceonstairsandthroughrailfaces", () -> OakBalustradeGameTests::diagonalRunsPlaceOnStairsAndThroughRailFaces);
        FUNCTIONS.register("oakbalustradegametests.cornersalignstackedpostsandreset", () -> OakBalustradeGameTests::cornersAlignStackedPostsAndReset);
        FUNCTIONS.register("oakbalustradegametests.onlytopplacementandpostssupportrail", () -> OakBalustradeGameTests::onlyTopPlacementAndPostsSupportRail);
        FUNCTIONS.register("oakrailinggametests.diagonaltopstransitionagainstnewels", () -> OakRailingGameTests::diagonalTopsTransitionAgainstNewels);
        FUNCTIONS.register("oakrailinggametests.insidecornersjoinandresetineveryorientation", () -> OakRailingGameTests::insideCornersJoinAndResetInEveryOrientation);
        FUNCTIONS.register("oakrailinggametests.outsidecornersplaceonrailendsandrequireanchors", () -> OakRailingGameTests::outsideCornersPlaceOnRailEndsAndRequireAnchors);
        FUNCTIONS.register("oakrailinggametests.horizontalrunstransitiontoeithersideofstairs", () -> OakRailingGameTests::horizontalRunsTransitionToEitherSideOfStairs);
        FUNCTIONS.register("oakrailinggametests.stairsteprunsalternateineverydirectionandplacementorder", () -> OakRailingGameTests::stairStepRunsAlternateInEveryDirectionAndPlacementOrder);
        FUNCTIONS.register("oakrailinggametests.straightanddiagonalonlyneighborsstayflat", () -> OakRailingGameTests::straightAndDiagonalOnlyNeighborsStayFlat);
        FUNCTIONS.register("oakrailinggametests.requireswallineveryorientation", () -> OakRailingGameTests::requiresWallInEveryOrientation);
        FUNCTIONS.register("ovengametests.ovenusesvanillafurnacebehaviorandlightswhileburning", () -> OvenGameTests::ovenUsesVanillaFurnaceBehaviorAndLightsWhileBurning);
        FUNCTIONS.register("refrigeratorgametests.refrigeratorbuildssixpartswithonepersistentdoublechestinventory", () -> RefrigeratorGameTests::refrigeratorBuildsSixPartsWithOnePersistentDoubleChestInventory);
        FUNCTIONS.register("shuttergametests.wallplacementusesfaceandclickedhinge", () -> ShutterGameTests::wallPlacementUsesFaceAndClickedHinge);
        FUNCTIONS.register("shuttergametests.joinedcolumntogglesfromanysection", () -> ShutterGameTests::joinedColumnTogglesFromAnySection);
        FUNCTIONS.register("shuttergametests.gapsandmismatchedhingesorfacingsstopjoining", () -> ShutterGameTests::gapsAndMismatchedHingesOrFacingsStopJoining);
        FUNCTIONS.register("shuttergametests.placementbridgesgroupsandextendsvertically", () -> ShutterGameTests::placementBridgesGroupsAndExtendsVertically);
        FUNCTIONS.register("shuttergametests.recipecraftseightandmaterialunlocksit", () -> ShutterGameTests::recipeCraftsEightAndMaterialUnlocksIt);
        FUNCTIONS.register("shuttergametests.everywoodjoinstogglesanddropsindependently", () -> ShutterGameTests::everyWoodJoinsTogglesAndDropsIndependently);
        FUNCTIONS.register("shuttergametests.breakinganysectiondropsonlythatshutter", () -> ShutterGameTests::breakingAnySectionDropsOnlyThatShutter);
        FUNCTIONS.register("sidetablegametests.sidetablehasfullheighttopsupport", () -> SideTableGameTests::sideTableHasFullHeightTopSupport);
        FUNCTIONS.register("sofagametests.sofaplacesbreaksandsupportsthreeindependentseats", () -> SofaGameTests::sofaPlacesBreaksAndSupportsThreeIndependentSeats);
        FUNCTIONS.register("tallpatiodoorgametests.keycuttercodespatiodoorandkeysoperateit", () -> TallPatioDoorGameTests::keyCutterCodesPatioDoorAndKeysOperateIt);
        FUNCTIONS.register("tallpatiodoorgametests.survivalplacementandeachsectiondropsonedoor", () -> TallPatioDoorGameTests::survivalPlacementAndEachSectionDropsOneDoor);
        FUNCTIONS.register("tallpatiodoorgametests.codeditempreserveskeydatawheneachsectionbreaks", () -> TallPatioDoorGameTests::codedItemPreservesKeyDataWhenEachSectionBreaks);
        FUNCTIONS.register("tallpatiodoorgametests.creativebreakingremoveswholedoorwithoutdrops", () -> TallPatioDoorGameTests::creativeBreakingRemovesWholeDoorWithoutDrops);
        FUNCTIONS.register("tallpatiodoorgametests.planksandglasscraftfourandunlockrecipe", () -> TallPatioDoorGameTests::planksAndGlassCraftFourAndUnlockRecipe);
        FUNCTIONS.register("toiletgametests.closedlidsupportssitting", () -> ToiletGameTests::closedLidSupportsSitting);
        FUNCTIONS.register("toiletgametests.openlidsupportssitting", () -> ToiletGameTests::openLidSupportsSitting);
        FUNCTIONS.register("windowaprongametests.baseboardcrossesapronsandclearsonremoval", () -> WindowApronGameTests::baseboardCrossesApronsAndClearsOnRemoval);
        FUNCTIONS.register("windowaprongametests.perpendicularbaseboardconnectsinfront", () -> WindowApronGameTests::perpendicularBaseboardConnectsInFront);
        FUNCTIONS.register("woodrailinggametests.everywoodsupportspostsstairsandwallmounts", () -> WoodRailingGameTests::everyWoodSupportsPostsStairsAndWallMounts);
    }

    private ModGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(ModGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "default"));
        var structure = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "chandelier_test");
        for (var function : FUNCTIONS.getEntries()) {
            event.registerTest(function.getId(), new FunctionGameTestInstance(function.unwrapKey().orElseThrow(),
                    new TestData<>(environment, structure, 100, 0, true)));
        }
    }
}
