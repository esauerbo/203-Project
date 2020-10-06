import java.util.*;

/*
EventScheduler: ideally our way of controlling what happens in our virtual world
 */

final class EventScheduler
{
   public static final int ATLANTIS_ANIMATION_PERIOD = 70;
   public static final int ATLANTIS_ANIMATION_REPEAT_COUNT = 7;
   public static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;



   public PriorityQueue<Event> eventQueue;
   public Map<Entity, List<Event>> pendingEvents;
   public double timeScale;

   public EventScheduler(double timeScale)
   {
      this.eventQueue = new PriorityQueue<>(new EventComparator());
      this.pendingEvents = new HashMap<>();
      this.timeScale = timeScale;
   }



   public void scheduleActions(Entity entity, WorldModel world, ImageStore imageStore)
   {
      switch (entity.kind)
      {
         case OCTO_FULL:
            this.scheduleEvent(entity, entity.createActivityAction(world, imageStore), entity.actionPeriod);
            this.scheduleEvent(entity, entity.createAnimationAction(0), entity.getAnimationPeriod());
            break;

         case OCTO_NOT_FULL:
            this.scheduleEvent(entity, entity.createActivityAction(world, imageStore), entity.actionPeriod);
            this.scheduleEvent(entity, entity.createAnimationAction(0), entity.getAnimationPeriod());
            break;

         case FISH:
            this.scheduleEvent(entity, entity.createActivityAction(world, imageStore), entity.actionPeriod);
            break;

         case CRAB:
            this.scheduleEvent(entity, entity.createActivityAction(world, imageStore), entity.actionPeriod);
            this.scheduleEvent(entity, entity.createAnimationAction(0), entity.getAnimationPeriod());
            break;

         case QUAKE:
            this.scheduleEvent(entity, entity.createActivityAction(world, imageStore), entity.actionPeriod);
            this.scheduleEvent(entity, entity.createAnimationAction(QUAKE_ANIMATION_REPEAT_COUNT), entity.getAnimationPeriod());
            break;

         case SGRASS:
            this.scheduleEvent(entity, entity.createActivityAction(world, imageStore), entity.actionPeriod);
            break;

         case ATLANTIS:
            this.scheduleEvent(entity, entity.createAnimationAction( ATLANTIS_ANIMATION_REPEAT_COUNT), entity.getAnimationPeriod());
            break;

         default:
      }
   }
   public boolean transformNotFull(Entity entity, WorldModel world, ImageStore imageStore)
   {
      if (entity.resourceCount >= entity.resourceLimit)
      {
         Entity octo = createOctoFull(entity.id, entity.resourceLimit,
                 entity.position, entity.actionPeriod, entity.animationPeriod,
                 entity.images);

         removeEntity(world, entity);
         unscheduleAllEvents(scheduler, entity);

         addEntity(world, octo);
         scheduleActions(octo, scheduler, world, imageStore);

         return true;
      }

      return false;
   }

   public void transformFull(Entity entity, WorldModel world, ImageStore imageStore)
   {
      Entity octo = createOctoNotFull(entity.id, entity.resourceLimit,
              entity.position, entity.actionPeriod, entity.animationPeriod,
              entity.images);

      world.removeEntity(entity);
      this.unscheduleAllEvents(entity);

      world.addEntity(octo);
      this.scheduleActions(octo, world, imageStore);
   }

   public static boolean moveToNotFull(Entity octo, WorldModel world,
                                       Entity target, EventScheduler scheduler)
   {
      if (octo.position.adjacent(target.position))
      {
         octo.resourceCount += 1;
         removeEntity(world, target);
         unscheduleAllEvents(scheduler, target);

         return true;
      }
      else
      {
         Point nextPos = octo.nextPositionOcto(world, target.position);

         if (!octo.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               unscheduleAllEvents(scheduler, occupant.get());
            }

            moveEntity(world, octo, nextPos);
         }
         return false;
      }
   }

   public static boolean moveToFull(Entity octo, WorldModel world,
                                    Entity target, EventScheduler scheduler)
   {
      if (octo.position.adjacent(target.position))
      {
         return true;
      }
      else
      {
         Point nextPos = octo.nextPositionOcto(world, target.position);

         if (!octo.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               unscheduleAllEvents(scheduler, occupant.get());
            }

            moveEntity(world, octo, nextPos);
         }
         return false;
      }
   }

   public static boolean moveToCrab(Entity crab, WorldModel world,
                                    Entity target, EventScheduler scheduler)
   {
      if (crab.position.adjacent(target.position))
      {
         removeEntity(world, target);
         unscheduleAllEvents(scheduler, target);
         return true;
      }
      else
      {
         Point nextPos = crab.nextPositionCrab(world, target.position);

         if (!crab.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               unscheduleAllEvents(scheduler, occupant.get());
            }

            moveEntity(world, crab, nextPos);
         }
         return false;
      }
   }

   public static void executeOctoFullActivity(Entity entity, WorldModel world,
                                              ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> fullTarget = findNearest(world, entity.position,
              EntityKind.ATLANTIS);

      if (fullTarget.isPresent() &&
              moveToFull(entity, world, fullTarget.get(), scheduler))
      {
         //at atlantis trigger animation
         scheduleActions(fullTarget.get(), scheduler, world, imageStore);

         //transform to unfull
         transformFull(entity, world, scheduler, imageStore);
      }
      else
      {
         scheduleEvent(scheduler, entity,
                 createActivityAction(entity, world, imageStore),
                 entity.actionPeriod);
      }
   }

   public void scheduleActions(Entity entity, WorldModel world, ImageStore imageStore)
   {
      switch (entity.kind)
      {
         case OCTO_FULL:
            this.scheduleEvent( entity,
                    entity.createActivityAction(world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent(entity, entity.createAnimationAction(0),
                    entity.getAnimationPeriod());
            break;

         case OCTO_NOT_FULL:
            this.scheduleEvent(entity,
                    entity.createActivityAction(world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent(entity,
                    entity.createAnimationAction(0),
                    entity.getAnimationPeriod());
            break;

         case FISH:
            this.scheduleEvent(entity,
                    entity.createActivityAction(world, imageStore),
                    entity.actionPeriod);
            break;

         case CRAB:
            this.scheduleEvent(entity,
                    entity.createActivityAction(world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent(entity,
                    entity.createAnimationAction(0),
                    entity.getAnimationPeriod());
            break;

         case QUAKE:
            this.scheduleEvent(entity,
                    entity.createActivityAction(world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent(entity,
                    entity.createAnimationAction(QUAKE_ANIMATION_REPEAT_COUNT),
                    entity.getAnimationPeriod());
            break;

         case SGRASS:
            this.scheduleEvent(entity,
                    entity.createActivityAction(world, imageStore),
                    entity.actionPeriod);
            break;
         case ATLANTIS:
            this.scheduleEvent(entity,
                    entity.createAnimationAction(ATLANTIS_ANIMATION_REPEAT_COUNT),
                    entity.getAnimationPeriod());
            break;

         default:
      }
   }


   public void scheduleEvent(Entity entity, Action action, long afterPeriod)
   {
      long time = System.currentTimeMillis() +
              (long)(afterPeriod * this.timeScale);
      Event event = new Event(action, time, entity);

      this.eventQueue.add(event);

      // update list of pending events for the given entity
      List<Event> pending = this.pendingEvents.getOrDefault(entity,
              new LinkedList<>());
      pending.add(event);
      this.pendingEvents.put(entity, pending);
   }



   public void unscheduleAllEvents(Entity entity)
   {
      List<Event> pending = this.pendingEvents.remove(entity);

      if (pending != null)
      {
         for (Event event : pending)
         {
            this.eventQueue.remove(event);
         }
      }
   }


   public void removePendingEvent(Event event)
   {
      List<Event> pending = this.pendingEvents.get(event.entity);

      if (pending != null)
      {
         pending.remove(event);
      }
   }



   public void updateOnTime(long time)
   {
      while (!this.eventQueue.isEmpty() &&
              this.eventQueue.peek().time < time)
      {
         Event next = this.eventQueue.poll();

         this.removePendingEvent(next);

         next.action.executeAction(this);
      }
   }

   public void executeActivityAction(Action action)
   {
      switch (action.entity.kind)
      {
         case OCTO_FULL:
            executeOctoFullActivity(action.entity, action.world,
                    action.imageStore, this);
            break;

         case OCTO_NOT_FULL:
            executeOctoNotFullActivity(action.entity, action.world,
                    action.imageStore, this);
            break;

         case FISH:
            executeFishActivity(action.entity, action.world, action.imageStore,
                    this);
            break;

         case CRAB:
            executeCrabActivity(action.entity, action.world,
                    action.imageStore, this);
            break;

         case QUAKE:
            executeQuakeActivity(action.entity, action.world, action.imageStore,
                    this);
            break;

         case SGRASS:
            executeSgrassActivity(action.entity, action.world, action.imageStore,
                    this);
            break;

         case ATLANTIS:
            executeAtlantisActivity(action.entity, action.world, action.imageStore,
                    this);
            break;

         default:
            throw new UnsupportedOperationException(
                    String.format("executeActivityAction not supported for %s",
                            action.entity.kind));
      }
   }

   public void executeActivityAction(Action action)
   {
      switch (action.entity.kind)
      {
         case OCTO_FULL:
            this.executeOctoFullActivity(action.entity, action.world, action.imageStore);
            break;

         case OCTO_NOT_FULL:
            this.executeOctoNotFullActivity(action.entity, action.world,
                    action.imageStore);
            break;

         case FISH:
            this.executeFishActivity(action.entity, action.world, action.imageStore);
            break;

         case CRAB:
            this.executeCrabActivity(action.entity, action.world,
                    action.imageStore);
            break;

         case QUAKE:
            this.executeQuakeActivity(action.entity, action.world, action.imageStore);
            break;

         case SGRASS:
            this.executeSgrassActivity(action.entity, action.world, action.imageStore);
            break;

         case ATLANTIS:
            this.executeAtlantisActivity(action.entity, action.world, this.imageStore);
            break;

         default:
            throw new UnsupportedOperationException(
                    String.format("executeActivityAction not supported for %s",
                            action.entity.kind));
      }
   }


   public void executeAnimationAction(Action action)
   {
      nextImage(action.entity);

      if (action.repeatCount != 1)
      {
         this.scheduleEvent(action.entity,
                 action.entity.createAnimationAction(Math.max(action.repeatCount - 1, 0)),
                 action.entity.getAnimationPeriod());
      }
   }

   public void executeAction(Action action)
   {
      switch (action.kind)
      {
         case ACTIVITY:
            this.executeActivityAction(action);
            break;

         case ANIMATION:
            this.executeAnimationAction(action);
            break;
      }
   }


}
