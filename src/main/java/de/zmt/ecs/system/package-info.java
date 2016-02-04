/**
 * Systems used in kitt.
 * <p>
 * Dependency graph showing execution order from top to bottom:
 * 
 * <pre>
 *          +--------------------+
 *          |SimulationTimeSystem|
 *          +----------+---------+
 *          ^          ^         ^
 *          |          |         |
 *          |          |         |
 * +--------++ +-------+------+ ++---------+
 * |AgeSystem| |BehaviorSystem| |FoodSystem|
 * +----+----+ +-+----------+-+ +---+------+
 *      ^        ^          ^       ^
 *      |        |          |       |
 *      |        |          |       |
 *      | +------+------+ +-+-------++
 *      | |ConsumeSystem| |MoveSystem|
 *      | +---+-----+---+ +-+-----+--+
 *      |     ^     ^       ^     ^
 *      +-----------+       |     |
 *            |     |       |     |
 * +----------+-+ +-+-------++ +--+------------+
 * |GrowthSystem| |FeedSystem| |MortalitySystem|
 * +---------+--+ +----------+ +---------------+
 *           ^
 *           |
 *           |
 * +---------+--------+
 * |ReproductionSystem|
 * +------------------+
 * </pre>
 * 
 * @see de.zmt.ecs.EntitySystem#getDependencies()
 * @author mey
 *
 */
package de.zmt.ecs.system;