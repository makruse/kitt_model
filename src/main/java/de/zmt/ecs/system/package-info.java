/**
 * Systems used in kitt.
 * <p>
 * Dependency graph showing execution order from top to bottom:
 * <p>
 * <img src="doc-files/gen/package-info.svg" alt=
 * "Systems Dependency Graph">
 * 
 * @see de.zmt.ecs.EntitySystem#getDependencies()
 * @author mey
 */
/*
@formatter:off
@startdot doc-files/gen/package-info.svg
digraph SystemsDependencyGraph {
        graph [rankdir=BT];
        FeedSystem -> AgeSystem;
        FeedSystem -> ConsumeSystem;
        AgeSystem -> SimulationTimeSystem;
        ConsumeSystem -> MoveSystem;
        MoveSystem -> BehaviorSystem;
        MoveSystem -> FoodSystem;
        ReproductionSystem -> GrowthSystem;
        GrowthSystem -> ConsumeSystem;
        BehaviorSystem -> SimulationTimeSystem;
        FoodSystem -> SimulationTimeSystem;
        MortalitySystem -> MoveSystem;
}

@enddot
@formatter:on
 */
package de.zmt.ecs.system;
