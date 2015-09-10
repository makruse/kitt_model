/**
 * Implementation of an Entity Component System (ECS) to be used with MASON,
 * adapted from Entity-System-RDBMS-Beta--Java-. Entities are Steppables that
 * trigger system updates to integrate them into MASON's schedule.
 * <p>
 * Components store the entity's state and do not really process anything.
 * Systems contain logic to manipulate state and require a certain set of
 * components to update an Entity. Components may have complex accessors to
 * manipulate their state but the act of manipulation is always triggered by
 * Systems. The idea is that state and logic is split between components and
 * systems.
 * <p>
 * Benefits:
 * <ul>
 * <li>Composition over Inheritance<br>
 * Mix any components, which cannot be achieved in the same way with
 * inheritance.</li>
 * <li>Decoupling<br>
 * Class bloat is prevented by splitting into several components and systems.
 * </li>
 * <li>Shared Data<br>
 * Systems share data by using the same components.</li>
 * </ul>
 * 
 * @see <a href="https://github.com/adamgit/Entity-System-RDBMS-Beta--Java-">
 *      Github: Entity-System-RDBMS-Beta--Java-</a>
 * @see <a href=
 *      "http://t-machine.org/index.php/2007/11/11/entity-systems-are-the-future-of-mmog-development-part-2/">
 *      T-Machine: Entity Systems are the future of MMOG development - Part
 *      2</a>
 * @author cmeyer
 * @author adam
 *
 */
package de.zmt.ecs;