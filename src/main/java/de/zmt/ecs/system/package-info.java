/**
 * Systems used in kitt.
 * <p>
 * Dependency graph showing execution order:
 * 
 * <pre>
 *          +--------------------+
 *          |SimulationTimeSystem|
 *          +---------+--+-------+
 *          ^         ^  ^----------+
 *          |         |             |
 * +--------+-----+ +-+-------+ +---+----------+
 * |GrowFoodSystem| |AgeSystem| |BehaviorSystem|
 * +-----------+--+ +-+-------+ +---+----------+
 *             ^      ^             ^
 *             |      |             |
 *           +-+------+-+       +---+------+
 *           |FeedSystem+------>|MoveSystem|
 *           +----------+       +---+------+
 *                ^                 ^
 *                |                 |
 *           +----+-------+     +---+-----------+
 *           |GrowthSystem|     |MortalitySystem|
 *           +------------+     +---------------+
 *                ^
 *                |
 *        +-------+----------+
 *        |ReproductionSystem|
 *        +------------------+
 * </pre>
 * 
 * @see de.zmt.ecs.EntitySystem#getDependencies()
 * @author mey
 *
 */
package de.zmt.ecs.system;