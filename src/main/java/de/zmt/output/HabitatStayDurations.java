package de.zmt.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.output.collectable.AbstractCollectable;
import de.zmt.output.collectable.CategoryCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.message.CollectMessage;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.strategy.MessageCollectStrategy;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.ParamDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 * Accumulates the stay durations for every habitat.
 * 
 * @author mey
 * 
 */
// TODO inherit from EnumCollectable
class HabitatStayDurations extends AbstractCollectable<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@link StrategyCollector} for collecting habitat stay
     * durations.
     * 
     * @param definitions
     *            the set of species definitions
     * @return the {@link StrategyCollector} for collecting habitat stay
     *         durations
     */
    public static StrategyCollector<?> createCollector(Collection<? extends SpeciesDefinition> definitions) {
        return StrategyCollector.create(new MyCategoryCollectable(definitions), new MyCollectStrategy());
    }

    private static final List<Habitat> HABITATS = Collections.unmodifiableList(Arrays.asList(Habitat.values()));
    private static final String HEADER_FORMAT_STRING = "%s_stay_" + UnitConstants.SIMULATION_TIME;
    private static final List<String> HEADERS = HABITATS.stream()
            // generate header names from format string
            .map(habitat -> String.format(HEADER_FORMAT_STRING, habitat)).collect(Collectors.toList());

    private final List<Amount<Duration>> amounts = new ArrayList<>(
            Collections.nCopies(HABITATS.size(), (Amount<Duration>) null));

    private HabitatStayDurations() {
        super(new ArrayList<>(Collections.nCopies(HABITATS.size(), (Long) null)));
        clear();
    }

    /**
     * Register stay for given habitat.
     * 
     * @param habitat
     */
    public void registerStay(Habitat habitat) {
        Amount<Duration> stepDuration = EnvironmentDefinition.STEP_DURATION;
        int index = habitat.ordinal();
        Amount<Duration> oldDuration = amounts.get(index);

        Amount<Duration> newDuration = oldDuration.plus(stepDuration);
        amounts.set(index, newDuration);
        getValues().set(index, newDuration.getExactValue());
    }

    /** Fill maps with zero durations. */
    @Override
    public void clear() {
        super.clear();

        Collections.fill(amounts, AmountUtil.zero(UnitConstants.SIMULATION_TIME));
    }

    @Override
    public List<String> obtainHeaders() {
        return HEADERS;
    }

    @Override
    protected Long obtainInitialValue() {
        return 0l;
    }

    private static class MyCategoryCollectable
            extends CategoryCollectable<ParamDefinition, HabitatStayDurations, Long> {
        private static final long serialVersionUID = 1L;

        public MyCategoryCollectable(Collection<? extends ParamDefinition> agentDefinitions) {
            super(agentDefinitions.stream()
                    .collect(Collectors.toMap(definition -> definition, definition -> new HabitatStayDurations())));
        }
    }

    private static class MyCollectStrategy extends MessageCollectStrategy<MyCategoryCollectable, HabitatMessage> {
        private static final long serialVersionUID = 1L;

        @Override
        protected void collect(HabitatMessage message, MyCategoryCollectable categoryCollectable) {
            SpeciesDefinition definition = message.entity.get(SpeciesDefinition.class);

            if (definition == null) {
                return;
            }

            HabitatStayDurations stayDurations = categoryCollectable.getCollectable(definition);

            if (stayDurations == null) {
                stayDurations = new HabitatStayDurations();
            }

            stayDurations.registerStay(message.habitat);
        }

        @Override
        protected CollectMessageFactory<HabitatMessage> getCollectMessageFactory() {
            return HabitatMessage.FACTORY;
        }

    }

    private static class HabitatMessage implements CollectMessage {
        public static final CollectMessageFactory<HabitatMessage> FACTORY = new Factory();

        private final Entity entity;
        private final Habitat habitat;

        public HabitatMessage(Entity entity, Habitat habitat) {
            super();
            this.entity = entity;
            this.habitat = habitat;
        }

        private static class Factory implements CollectMessageFactory<HabitatMessage> {

            @Override
            public Stream<HabitatMessage> createCollectMessages(SimState state) {
                Entity environment = ((Kitt) state).getEnvironment();
                return AgentCollectMessage.FACTORY.createCollectMessages(state)
                        .map(message -> createHabitatMessage(message.getSimObject(), environment));
            }

            /**
             * Creates a {@link HabitatMessage} with the given data.
             * 
             * @param agent
             * @param environment
             * @return {@link HabitatMessage} with given data
             */
            private static HabitatMessage createHabitatMessage(Entity agent, Entity environment) {
                Double2D position = agent.get(Moving.class).getPosition();
                HabitatMap habitatMap = environment.get(HabitatMap.class);
                WorldToMapConverter converter = environment.get(EnvironmentDefinition.class);
                Habitat habitat = habitatMap.obtainHabitat(position, converter);
                return new HabitatMessage(agent, habitat);
            }

        }
    }
}