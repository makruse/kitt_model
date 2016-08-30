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
        "GrowthSystem" -> {"ConsumeSystem" "StepSkipSystem" }
        "FeedSystem" -> {"ConsumeSystem" "MoveSystem" "AgeSystem" "StepSkipSystem" }
        "BehaviorSystem" -> {"SimulationTimeSystem" }
        "StepSkipSystem" -> {}
        "SimulationTimeSystem" -> {}
        "FoodSystem" -> {"SimulationTimeSystem" }
        "MortalitySystem" -> {"MoveSystem" "StepSkipSystem" }
        "ReproductionSystem" -> {"GrowthSystem" }
        "MoveSystem" -> {"FoodSystem" "BehaviorSystem" "StepSkipSystem" }
        "AgeSystem" -> {"StepSkipSystem" }
        "ConsumeSystem" -> {"MoveSystem" "BehaviorSystem" "StepSkipSystem" }
}

@enddot
@formatter:on
 */
package de.zmt.ecs.system;
