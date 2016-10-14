/**
 * Systems used in kitt.
 * <p>
 * Dependency graph showing execution order from top to bottom:
 * <p>
 * <img src="doc-files/gen/package-info.svg" alt= "Systems Dependency Graph">
 * 
 * @see de.zmt.ecs.EntitySystem#getDependencies()
 * @author mey
 */
/*
@formatter:off
@startdot doc-files/gen/package-info.svg
digraph SystemsDependencyGraph {
        graph [rankdir=BT];
        "AgeSystem" -> "MoveSystem";
        "MoveSystem" -> "BehaviorSystem";
        "ConsumeSystem" -> "AgeSystem";
        "FeedSystem" -> "GrowthSystem";
        "GrowthSystem" -> "ConsumeSystem";
        "MortalitySystem" -> "MoveSystem";
        "ReproductionSystem" -> "GrowthSystem";
        "MetamorphosisSystem";
        "FoodSystem" -> "SimulationTimeSystem";
}
@enddot
@formatter:on
 */
package de.zmt.ecs.system;
