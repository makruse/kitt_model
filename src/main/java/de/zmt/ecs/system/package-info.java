/**
 * Systems used in kitt.
 * <p>
 * Dependency graph showing execution order from top to bottom:
 * 
 * <pre>
 *       +--------------------+
 *       |SimulationTimeSystem|
 *       +--------------------+
 *         ^        ^        ^
 *         |        |        |
 * +-------+-+ +----+-----+ ++-------------+
 * |AgeSystem| |FoodSystem| |BehaviorSystem|
 * +---------+ +----------+ +--------------+
 *          ^          ^      ^
 *          |          |      |
 *          |       +--+------++
 *          |       |MoveSystem|
 *          |       +----------+
 *          |        ^        ^
 *          |        |        |
 *         ++--------++   +---+-----------+
 *         |FeedSystem|   |MortalitySystem|
 *         +----------+   +---------------+
 *          ^
 *          |
 * +--------+----+
 * |ConsumeSystem|
 * +-------------+
 *          ^
 *          |
 * +--------+---+
 * |GrowthSystem|
 * +------------+
 *          ^
 *          |
 * +--------+---------+
 * |ReproductionSystem|
 * +------------------+
 * </pre>
 * 
 * @see de.zmt.ecs.EntitySystem#getDependencies()
 * @author mey
 *
 */
package de.zmt.ecs.system;