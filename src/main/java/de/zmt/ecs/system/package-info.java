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
        "BehaviorSystem" -> {"SimulationTimeSystem" }
        "GrowthSystem" -> {"ConsumeSystem" }
        "MoveSystem" -> {"BehaviorSystem" "FoodSystem" }
        "FeedSystem" -> {"ConsumeSystem" "AgeSystem" }
        "SimulationTimeSystem" -> {}
        "ConsumeSystem" -> {"BehaviorSystem" "MoveSystem" }
        "MortalitySystem" -> {"MoveSystem" }
        "ReproductionSystem" -> {"GrowthSystem" }
        "AgeSystem" -> {"MoveSystem" }
        "FoodSystem" -> {"SimulationTimeSystem" }
}
@enddot
@formatter:on
 */
package de.zmt.ecs.system;
