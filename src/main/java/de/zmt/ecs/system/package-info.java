/**
 * Systems used in kitt.
 * <p>
 * Dependency graph showing execution order from top to bottom:
 * 
 * <pre>
 *           +--------------------+
 *           |SimulationTimeSystem|
 *           ++---------+---------+
 *            ^         ^         ^
 *            |         |         |
 *            |         |         |
 *            | +-------+------+ ++---------+
 *            | |BehaviorSystem| |FoodSystem|
 *            | +------------+-+ +---+------+
 *            |              ^       ^
 *            |              |       |
 *            |              |       |
 *         +--+------+     +-+-------++
 *         |AgeSystem|     |MoveSystem|
 *         +-+-------+     +-+-----+--+
 *           ^               ^     ^
 *           |               |     |
 *           |               |     |
 *           |  +------------++ +--+------------+
 *           |  |ConsumeSystem| |MortalitySystem|
 *           |  +-+-------+---+ +---------------+
 *           |    ^       ^
 *           |    |       |
 *           |    |       |
 *      +----+----++ +----+-------+
 *      |FeedSystem| |GrowthSystem|
 *      +----------+ +---------+--+
 *                        ^
 *                        |
 *                        |
 *              +---------+--------+
 *              |ReproductionSystem|
 *              +------------------+
 * </pre>
 * 
 * @see de.zmt.ecs.EntitySystem#getDependencies()
 * @author mey
 *
 */
package de.zmt.ecs.system;