package gigaherz.woodworking.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.types.Type;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.network.FMLPlayMessages;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegSitter
{
    private final List<DeferredRegister<?>> registerList = Lists.newArrayList();
    private final NonNullLazy<DeferredRegister<Block>> BLOCKS = NonNullLazy.of(() -> createDeferred(ForgeRegistries.BLOCKS));
    private final NonNullLazy<DeferredRegister<Item>> ITEMS = NonNullLazy.of(() -> createDeferred(ForgeRegistries.ITEMS));
    private final NonNullLazy<DeferredRegister<BlockEntityType<?>>> BLOCK_ENTITIES = NonNullLazy.of(() -> createDeferred(ForgeRegistries.BLOCK_ENTITIES));
    private final NonNullLazy<DeferredRegister<SoundEvent>> SOUND_EVENTS = NonNullLazy.of(() -> createDeferred(ForgeRegistries.SOUND_EVENTS));
    private final NonNullLazy<DeferredRegister<Enchantment>> ENCHANTMENTS = NonNullLazy.of(() -> createDeferred(ForgeRegistries.ENCHANTMENTS));
    private final NonNullLazy<DeferredRegister<EntityType<?>>> ENTITIES = NonNullLazy.of(() -> createDeferred(ForgeRegistries.ENTITIES));
    private final String modId;

    public RegSitter(String modId)
    {
        this.modId = modId;
    }

    private <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createDeferred(IForgeRegistry<T> registry)
    {
        DeferredRegister<T> deferred = DeferredRegister.create(registry, RegSitter.this.modId);
        registerList.add(deferred);
        return deferred;
    }

    public final void subscribeEvents(IEventBus bus)
    {
        registerList.forEach(def -> def.register(bus));
    }

    /////////////////////////////////////////////////////////
    // Helpers to get existing items

    public <T extends Block> RegistryObject<T> block(String name)
    {
        return RegistryObject.of(new ResourceLocation(modId, name), ForgeRegistries.BLOCKS);
    }

    public <T extends Item> RegistryObject<T> item(String name)
    {
        return RegistryObject.of(new ResourceLocation(modId, name), ForgeRegistries.ITEMS);
    }

    public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> tileEntity(String name)
    {
        return RegistryObject.of(new ResourceLocation(modId, name), ForgeRegistries.BLOCK_ENTITIES);
    }

    /////////////////////////////////////////////////////////
    // Helpers to register new things

    public final <T extends Block> MiniBlock<T> block(String name, Supplier<T> factory)
    {
        return new MiniBlock<>(name, factory);
    }

    public final <T extends Item> MiniGeneric<T> item(String name, Supplier<T> factory)
    {
        return new MiniGeneric<>(ITEMS, name, factory);
    }

    @SafeVarargs
    public final <T extends BlockEntity> MiniTileEntity<T> tileEntity(String name, BlockEntityType.BlockEntitySupplier<T> factory, RegistryObject<? extends Block>... blocks)
    {
        return new MiniTileEntity<T>(BLOCK_ENTITIES, name, factory, ImmutableSet.copyOf(blocks));
    }

    public final <T extends SoundEvent> MiniGeneric<T> soundEvent(String name, Supplier<T> factory)
    {
        return new MiniGeneric<>(SOUND_EVENTS, name, factory);
    }

    public final <T extends Enchantment> MiniGeneric<T> enchantment(String name, Supplier<T> factory)
    {
        return new MiniGeneric<>(ENCHANTMENTS, name, factory);
    }

    public final <T extends Entity> MiniEntity<T> entityType(String name, EntityType.EntityFactory<T> factory, MobCategory classification)
    {
        return new MiniEntity<>(ENTITIES, name, factory, classification);
    }

    /////////////////////////////////////////////////////////
    // Helper implementations

    public class MiniBlock<T extends Block> extends MiniGeneric<T>
    {
        private Function<Supplier<T>, ? extends Item> itemFactory;
        private Function<Supplier<T>, ? extends BlockEntityType<?>> tileEntityFactory;

        private MiniBlock(String name, Supplier<T> factory)
        {
            super(BLOCKS, name, factory);
        }

        public MiniBlock<T> withItem()
        {
            return withItem(new Item.Properties());
        }

        public MiniBlock<T> withItem(Item.Properties properties)
        {
            return withItem((block) -> new BlockItem(block.get(), properties));
        }

        public MiniBlock<T> withItem(Function<Supplier<T>, ? extends Item> itemFactory)
        {
            this.itemFactory = itemFactory;
            return this;
        }

        public <E extends BlockEntity> MiniBlock<T> withBlockEntity(BlockEntityType.BlockEntitySupplier<E> factory)
        {
            return withBlockEntity((block) -> new BlockEntityType<>(factory, ImmutableSet.of(block.get()), null));
        }

        public <E extends BlockEntity> MiniBlock<T> withBlockEntity(BlockEntityType.BlockEntitySupplier<E> factory, Function<Supplier<T>, Collection<Supplier<? extends Block>>> validBlocks)
        {
            return withBlockEntity((block) -> new BlockEntityType<>(factory, ImmutableSet.copyOf(validBlocks.apply(block).stream().map(Supplier::get).iterator()), null));
        }

        public MiniBlock<T> withBlockEntity(Function<Supplier<T>, ? extends BlockEntityType<?>> tileEntityFactory)
        {
            this.tileEntityFactory = tileEntityFactory;
            return this;
        }

        public RegistryObject<T> defer()
        {
            RegistryObject<T> block = super.defer();
            if (itemFactory != null)
                ITEMS.get().register(name, () -> itemFactory.apply(block));
            if (tileEntityFactory != null)
                BLOCK_ENTITIES.get().register(name, () -> tileEntityFactory.apply(block));
            return block;
        }
    }

    public class MiniTileEntity<T extends BlockEntity> extends MiniGeneric<BlockEntityType<T>>
    {
        private final BlockEntityType.BlockEntitySupplier<? extends T> factory;
        private final Set<RegistryObject<? extends Block>> blocks;
        private Type<?> dataFixerType = null;

        private MiniTileEntity(NonNullSupplier<? extends DeferredRegister<? super BlockEntityType<T>>> deferred, String name, BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<RegistryObject<? extends Block>> blocks)
        {
            super(deferred, name, null);
            this.factory = factory;
            this.blocks = blocks;
        }

        @Override
        protected Supplier<? extends BlockEntityType<T>> factory()
        {
            return this::build;
        }

        private BlockEntityType<T> build()
        {
            return new BlockEntityType<>(factory, ImmutableSet.copyOf(blocks.stream().map(RegistryObject::get).iterator()), this.dataFixerType);
        }
    }

    public class MiniEntity<T extends Entity> extends MiniGeneric<EntityType<T>>
    {
        private EntityType.Builder<T> builder;

        private MiniEntity(NonNullSupplier<? extends DeferredRegister<? super EntityType<T>>> deferred, String name, EntityType.EntityFactory<T> factory, MobCategory classification)
        {
            super(deferred, name, null);
            this.builder = EntityType.Builder.of(factory, classification);
        }

        @Override
        protected Supplier<? extends EntityType<T>> factory()
        {
            return this::build;
        }

        private EntityType<T> build()
        {
            return builder.build(new ResourceLocation(modId, name).toString());
        }

        public MiniEntity<T> size(float width, float height)
        {
            builder.sized(width, height);
            return this;
        }

        public MiniEntity<T> disableSummoning()
        {
            builder.noSummon();
            return this;
        }

        public MiniEntity<T> disableSerialization()
        {
            builder.noSave();
            return this;
        }

        public MiniEntity<T> immuneToFire()
        {
            builder.fireImmune();
            return this;
        }

        public MiniEntity<T> spawnableFarFromPlayer()
        {
            builder.canSpawnFarFromPlayer();
            return this;
        }

        public MiniEntity<T> setUpdateInterval(int interval)
        {
            builder.setUpdateInterval(interval);
            return this;
        }

        public MiniEntity<T> setTrackingRange(int range)
        {
            builder.setTrackingRange(range);
            return this;
        }

        public MiniEntity<T> setShouldReceiveVelocityUpdates(boolean value)
        {
            builder.setShouldReceiveVelocityUpdates(value);
            return this;
        }

        public MiniEntity<T> setCustomClientFactory(BiFunction<FMLPlayMessages.SpawnEntity, Level, T> customClientFactory)
        {
            builder.setCustomClientFactory(customClientFactory);
            return this;
        }
    }

    public static class MiniGeneric<T extends IForgeRegistryEntry<? super T>>
    {
        private final NonNullSupplier<? extends DeferredRegister<? super T>> deferred;
        private final Supplier<? extends T> factory;

        protected final String name;

        private MiniGeneric(NonNullSupplier<? extends DeferredRegister<? super T>> deferred, String name, Supplier<? extends T> factory)
        {
            this.deferred = deferred;
            this.name = name;
            this.factory = factory;
        }

        protected Supplier<? extends T> factory()
        {
            return factory;
        }

        public RegistryObject<T> defer()
        {
            return deferred.get().register(name, factory());
        }
    }
}