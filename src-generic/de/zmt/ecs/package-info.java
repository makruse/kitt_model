/**
 * Implementation of an Entity Component System to be used with MASON, adapted
 * from Entity-System-RDBMS-Beta--Java-. Entities are Steppables that trigger
 * system updates to integrate them into MASON's schedule. 
 * <p>
 * Benefits:
 * <ul>
 * <li>Composition over Inheritance<br>
 * Mix any components, which cannot be 
 * achieved in the same way with inheritance.</li>
 * <li>Decoupling<br>
 * Class bloat is prevented by splitting into several components
 * and systems.</li>
 * <li>Shared Data<br>Systems share data by using the same components.</li>
 * </ul>
 * 
 * @see <a href=https://github.com/adamgit/Entity-System-RDBMS-Beta--Java->Github: Entity-System-RDBMS-Beta--Java-</a>
 * @author cmeyer
 * @author adam
 *
 */
package de.zmt.ecs;