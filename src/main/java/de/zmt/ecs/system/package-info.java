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
        rankdir=BT
        "GrowthSystem" -> {"ConsumeSystem" }
        "ConsumeSystem" -> {"BehaviorSystem" "MoveSystem" }
        "SimulationTimeSystem" -> {}
        "BehaviorSystem" -> {}
        "MortalitySystem" -> {"MoveSystem" }
        "FoodSystem" -> {"SimulationTimeSystem" }
        "MetamorphosisSystem" -> {}
        "FeedSystem" -> {"ConsumeSystem" "AgeSystem" }
        "MoveSystem" -> {"BehaviorSystem" }
        "ReproductionSystem" -> {"GrowthSystem" }
        "AgeSystem" -> {"MoveSystem" }
}
@enddot
@formatter:on
 */
package de.zmt.ecs.system;
