package gg.norisk.heroes.spiderman.grapple;

import gg.norisk.heroes.spiderman.Manager;
import gg.norisk.heroes.spiderman.entity.WebEntity;
import gg.norisk.heroes.spiderman.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.silkmc.silk.core.entity.MovementExtensionsKt;

import java.util.HashSet;


/*
Full Credits to https://github.com/yyon/grapplemod
 */
public class GrapplingHookPhysicsController {
    public static final Identifier GRAPPLING_HOOK_CONTROLLER = Manager.INSTANCE.toId("grappling_hook");

    public int entityId;
    public World world;
    public Entity entity;

    private int lastTickRan = -1;
    private int duplicates = 0;

    private final HashSet<WebEntity> grapplehookEntities = new HashSet<>();
    private final HashSet<Integer> grapplehookEntityIds = new HashSet<>();

    private boolean isControllerActive = true;

    protected Vec motion;

    protected double playerForward = 0;
    protected double playerStrafe = 0;
    protected boolean playerJump = false;
    protected boolean playerSneak = false;
    protected Vec playerMovementUnrotated = new Vec(0, 0, 0);
    protected Vec playerMovement = new Vec(0, 0, 0);

    protected int onGroundTimer;
    protected int maxOnGroundTimer = 3;

    protected double maxLen = 60;

    protected double playerMovementMult = 1;

    private final double repelMaxPush = 0.3;

    private boolean rocketKeyDown = false;
    private double rocketProgression;

    private int ticksSinceLastWallrunSoundEffect = 0;

    private boolean isOnWall = false;
    private Vec wallDirection = null;
    private BlockHitResult wallrunRaytraceResult = null;

    public GrapplingHookPhysicsController(int grapplehookEntityId, int entityId, World world) {
        this.entityId = entityId;
        this.world = world;

        this.entity = world.getEntityById(entityId);

        if (this.entity == null || !this.entity.isAlive()) {
            Manager.INSTANCE.getLogger().warn("GrapplingHookPhysicsController is missing an expected holder entity!");
            this.disable();
            return;
        }

        this.motion = Vec.motionVec(this.entity);

        // undo friction
        Vec newmotion = new Vec(entity.getPos().x - entity.lastRenderX, entity.getPos().y - entity.lastRenderY, entity.getPos().z - entity.lastRenderZ);
        if (newmotion.x / motion.x < 2 && motion.x / newmotion.x < 2 && newmotion.y / motion.y < 2 && motion.y / newmotion.y < 2 && newmotion.z / motion.z < 2 && motion.z / newmotion.z < 2) {
            this.motion = newmotion;
        }

        this.onGroundTimer = 0;

        if (grapplehookEntityId != -1) {
            Entity grapplehookEntity = world.getEntityById(grapplehookEntityId);
            if (grapplehookEntity != null && grapplehookEntity.isAlive() && grapplehookEntity instanceof WebEntity grapple) {
                this.addHookEntity(grapple);

            } else {
                Manager.INSTANCE.getLogger().warn("GrapplingHookPhysicsController is missing an expected hook entity!");
                this.disable();
            }
        }
    }

    public Identifier getType() {
        return GRAPPLING_HOOK_CONTROLLER;
    }

    public void disable() {
        entity.sendMessage(Text.of("Removing..."));
        GrappleModUtils.INSTANCE.setController(null);
        // Error'ed controllers should just be removed with no extra
        // conntrollers applied - they should be 'disabled' already.

        boolean wasAlreadyDisabled = !this.isControllerActive;
        this.isControllerActive = false;

        PlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        boolean isEntityClientPlayer = this.entity == clientPlayer;

        // Not null & player
        // Reset server-side physics tracking.
        /*if (isEntityClientPlayer && !wasAlreadyDisabled)
            NetworkManager.packetToServer(new PhysicsUpdateMessage());


        if (GrappleModClient.get().getClientControllerManager().unregisterController(this.entityId) == null)
            return;

        if (this.getType() == AIR_FRICTION_CONTROLLER)
            return;

        NetworkManager.packetToServer(new GrappleEndMessage(this.entityId, this.grapplehookEntityIds));

        if (this.entity instanceof LocalPlayer p) {
            PlayerInfo playerInfo = p.connection.getPlayerInfo(p.getUUID());

            if (playerInfo != null && playerInfo.getGameMode() == GameType.SPECTATOR) return;
        }

        if (!wasAlreadyDisabled) {
            GrappleModClient.get()
                    .getClientControllerManager()
                    .createControl(AIR_FRICTION_CONTROLLER, -1, this.entityId, this.entity.getWorld(), null, this.custom);
        } */
    }


    public void doClientTick() {
        entity.sendMessage(Text.of("Ticking..."));

        if (!this.isControllerActive) {
            this.disable();
            return;
        }

        IntegratedServer server = MinecraftClient.getInstance().getServer();

        if (server != null) {
            int serverTick = server.getTicks();

            if (serverTick == lastTickRan) this.duplicates++;
            else this.duplicates = 0;

            this.lastTickRan = serverTick;
        }

        if (this.entity == null || !this.entity.isAlive() || grapplehookEntities.stream().noneMatch(Entity::isAlive)) {
            this.disable();
        } else {
            this.updatePlayerPos();
            this.transmitServerPhysicsUpdate();
        }
    }

    public void transmitServerPhysicsUpdate() {
        if (!this.isControllerActive)
            return;

        PlayerEntity clientPlayer = MinecraftClient.getInstance().player;

        if (this.entity == null)
            return;

        if (this.entity != clientPlayer)
            return;

        /*PlayerPhysicsFrame frame = new PlayerPhysicsFrame()
                .setPhysicsControllerType(this.getType())
                .setSpeed(this.motion.length())
                .setUsingRocket(this.rocketKeyDown);

        NetworkManager.packetToServer(new PhysicsUpdateMessage(frame));*/
    }

    public void receivePlayerMovementMessage(float strafe, float forward, boolean sneak) {
        entity.sendMessage(Text.of("Strafe: " + strafe + " Forward " + forward + " Sneak " + sneak));
        this.playerForward = forward;
        this.playerStrafe = strafe;
        this.playerSneak = sneak;
        this.playerMovementUnrotated = new Vec(strafe, 0, forward);
        this.playerMovement = playerMovementUnrotated.rotateYaw((float) (this.entity.getYaw() * (Math.PI / 180.0)));
    }

    public void updatePlayerPos() {
        Entity entity = this.entity;

        if (!this.isControllerActive) return;
        if (entity == null) return;

        if (entity.getVehicle() != null) {
            this.disable();
            this.updateServerPos();
            return;
        }

        this.normalGround(false);
        this.normalCollisions(false);
        this.applyAirFriction();

        Vec playerPos = Vec.positionVec(entity).add(new Vec(0, entity.getStandingEyeHeight(), 0));
        Vec additionalMotion = new Vec(0, 0, 0);
        Vec gravity = new Vec(0, -0.05, 0);

        this.motion.mutableAdd(gravity);


        Vec averagemotiontowards = new Vec(0, 0, 0);
        double minSphereVecDist = 99999;
        double jumpSpeed = 0;
        boolean close = false;
        boolean doJump = false;
        boolean isClimbing = false;

        for (WebEntity hookEntity : this.grapplehookEntities) {
            Vec hookPos = Vec.positionVec(hookEntity);
            RopeSegmentHandler segmentHandler = hookEntity.getSegmentHandler();


            segmentHandler.update(hookPos, playerPos, hookEntity.getRopeLength(), false);

            // vectors along rope
            Vec anchor = segmentHandler.getClosest(hookPos);
            double distToAnchor = segmentHandler.getDistToAnchor();
            double remainingLength = hookEntity.getRopeLength() - distToAnchor;

            Vec oldspherevec = playerPos.sub(anchor);
            Vec spherevec = oldspherevec.withMagnitude(remainingLength);
            Vec spherechange = spherevec.sub(oldspherevec);

            if (spherevec.length() < minSphereVecDist) {
                minSphereVecDist = spherevec.length();
            }

            averagemotiontowards.mutableAdd(spherevec.withMagnitude(-1));

            // snap to rope length
            if (oldspherevec.length() >= remainingLength) {
                if (oldspherevec.length() - remainingLength > GrappleSettings.INSTANCE.getRope_snap_buffer()) {
                    // if rope is too long, the rope snaps

                    this.disable();
                    this.updateServerPos();
                    return;
                } else {
                    additionalMotion = spherechange.scale(0.8f);
                }
            }

            double playerToAnchorDist = oldspherevec.length();

            this.applyCalculatedTaut(playerToAnchorDist, hookEntity);

            // handle keyboard input (jumping and climbing)
            if (entity instanceof PlayerEntity player) {
                boolean detachKeyDown = GrappleKey.DETACH.isPressed();
                boolean isJumping = detachKeyDown && !this.playerJump;
                this.playerJump = detachKeyDown;

                if (isJumping && this.onGroundTimer >= 0) {
                    // jumping
                    /*double timer = GrappleModClient.get().getTimeSinceLastRopeJump(this.entity.getWorld());
                    if (timer > GrappleSettings.INSTANCE.getRope_jump_cooldown_s() * 20.0) {
                        doJump = true;
                        jumpSpeed = this.getJumpPower(player, spherevec, hookEntity);
                    }*/
                }

                if (GrappleKey.DAMPEN_SWING.isPressed()) {
                    // slow down
                    Vec motiontorwards = spherevec.withMagnitude(-0.1);
                    motiontorwards = new Vec(motiontorwards.x, 0, motiontorwards.z);

                    if (this.motion.dot(motiontorwards) < 0)
                        this.motion.mutableAdd(motiontorwards);

                    Vec newmotion = this.dampenMotion(this.motion, motiontorwards);
                    this.motion = new Vec(newmotion.x, this.motion.y, newmotion.z);

                }

                if ((GrappleKey.CLIMB.isPressed() || GrappleKey.CLIMB_UP.isPressed() || GrappleKey.CLIMB_DOWN.isPressed()) /*&& !motor*/) {
                    Vec climbMotion = anchor.y != playerPos.y
                            ? this.calculateClimbingMotion(hookEntity, playerToAnchorDist, distToAnchor, spherevec)
                            : new Vec(0, 0, 0);

                    isClimbing = true;
                    additionalMotion.mutableAdd(climbMotion);
                }
            }

            if (playerToAnchorDist + distToAnchor < 2) {
                close = true;
            }

            // swing along max rope length
            if (anchor.sub(playerPos.add(motion)).length() > remainingLength) { // moving away
                this.motion = this.motion.removeAlong(spherevec);
            }
        }

        averagemotiontowards.mutableSetMagnitude(1);

        Vec facing = new Vec(entity.getRotationVector()).normalize();

        // WASD movement
        if (!doJump && !isClimbing) {
            this.applyPlayerMovement();
        }

        // jump
        if (doJump) {
            double maxJumpPower = GrappleSettings.INSTANCE.getRope_jump_power();
            jumpSpeed = MathHelper.clamp(jumpSpeed, 0.0D, maxJumpPower);

            this.doJump(entity, jumpSpeed, averagemotiontowards, minSphereVecDist);
            //GrappleModClient.get().resetRopeJumpTime(this.entity.getWorld());
            return;
        }

        // now to actually apply everything to the player
        Vec newmotion = motion.add(additionalMotion);

        if (Double.isNaN(newmotion.x) || Double.isNaN(newmotion.y) || Double.isNaN(newmotion.z)) {
            newmotion = new Vec(0, 0, 0);
            this.motion = new Vec(0, 0, 0);
            Manager.INSTANCE.getLogger().warn("error: motion is NaN");
        }

        entity.setVelocity(newmotion.x, newmotion.y, newmotion.z);

        this.updateServerPos();
    }

    private Vec calculateClimbingMotion(WebEntity hook, double dist, double distToAnchor, Vec spherevec) {
        // climb up/down rope
        double climbDelta = 0;

        if (GrappleKey.CLIMB.isPressed()) {
            climbDelta = this.playerForward;

            if (GrappleModUtils.INSTANCE.isMovingSlowly(this.entity))
                climbDelta /= 0.3D;

            climbDelta = MathHelper.clamp(climbDelta, -1.0D, 1.0D);

        } else if (GrappleKey.CLIMB_UP.isPressed()) {
            climbDelta = 1.0D;
        } else if (GrappleKey.CLIMB_DOWN.isPressed()) {
            climbDelta = -1.0D;
        }


        if (climbDelta == 0) return new Vec(0, 0, 0);

        double climbSpeed = GrappleSettings.INSTANCE.getClimb_speed();

        if (dist + distToAnchor >= this.maxLen && climbDelta <= 0 && this.maxLen != 0)
            return new Vec(0, 0, 0);

        hook.setRopeLength(dist + distToAnchor);
        hook.setRopeLength(hook.getRopeLength() - climbDelta * climbSpeed);

        if (hook.getRopeLength() < distToAnchor) {
            hook.setRopeLength(dist + distToAnchor);
        }

        Vec up = new Vec(0, 1, 0);
        Vec additionalVerticalMovement = spherevec.withMagnitude(-climbDelta * climbSpeed).project(up);

        if (additionalVerticalMovement.y > 0)
            additionalVerticalMovement.mutableScale(0.66f);

        return additionalVerticalMovement;
    }

    public void applyCalculatedTaut(double dist, WebEntity hookEntity) {
        if (hookEntity == null) return;

        hookEntity.setTaut(dist < hookEntity.getRopeLength()
                ? Math.max(0, 1 - ((hookEntity.getRopeLength() - dist) / 5))
                : 1.0d);
    }

    public void normalCollisions(boolean sliding) {

        // stop if collided with object
        if (this.entity.horizontalCollision) {
            if (this.entity.getVelocity().x == 0) {
                if (!sliding || this.tryStepUp(new Vec(this.motion.x, 0, 0))) {
                    this.motion.x = 0;
                }
            }

            if (this.entity.getVelocity().z == 0) {
                if (!sliding || this.tryStepUp(new Vec(0, 0, this.motion.z))) {
                    this.motion.z = 0;
                }
            }
        }

        if (sliding && !this.entity.horizontalCollision) {
            if (entity.getPos().x - entity.lastRenderX == 0) {
                this.motion.x = 0;
            }
            if (entity.getPos().z - entity.lastRenderZ == 0) {
                this.motion.z = 0;
            }
        }

        if (this.entity.verticalCollision) {
            if (this.entity.isOnGround()) {
                if (!sliding && MinecraftClient.getInstance().options.jumpKey.isPressed()) {
                    this.motion.y = entity.getVelocity().y;
                } else {
                    if (this.motion.y < 0) {
                        this.motion.y = 0;
                    }
                }

            } else {
                if (this.motion.y > 0 && entity.lastRenderY == entity.getPos().y) {
                    this.motion.y = 0;
                }
            }
        }
    }

    public boolean tryStepUp(Vec collisionMotion) {
        if (collisionMotion.length() == 0)
            return false;

        Vec moveOffset = collisionMotion.withMagnitude(0.05).add(0, entity.getStepHeight() + 0.01, 0);
        Iterable<VoxelShape> collisions = this.entity.getWorld().getCollisions(this.entity, this.entity.getBoundingBox().offset(moveOffset.x, moveOffset.y, moveOffset.z));

        if (collisions.iterator().hasNext()) return true;

        if (this.entity.isOnGround()) {
            this.entity.horizontalCollision = false;
            return false;
        }

        Vec pos = Vec.positionVec(entity);
        pos.mutableAdd(moveOffset);
        pos.applyAsPositionTo(entity);
        this.entity.lastRenderX = pos.x;
        this.entity.lastRenderY = pos.y;
        this.entity.lastRenderZ = pos.z;

        return false;
    }

    public void normalGround(boolean sliding) {
        if (this.entity.isOnGround()) {
            this.onGroundTimer = this.maxOnGroundTimer;

        } else if (this.onGroundTimer > 0) {
            this.onGroundTimer--;
        }

        boolean touchingGround = this.entity.isOnGround() || this.onGroundTimer > 0;

        if (touchingGround && !sliding) {
            this.motion = Vec.motionVec(this.entity);
            GameOptions options = MinecraftClient.getInstance().options;

            if (options.jumpKey.isPressed())
                this.motion.y += 0.05;
        }
    }

    private double getJumpPower(Entity player, double jumppower) {
        double maxjump = GrappleSettings.INSTANCE.getRope_jump_power();
        if (onGroundTimer > 0) { // on ground: jump normally
            onGroundTimer = 20;
            return 0;
        }
        if (player.isOnGround()) {
            jumppower = 0;
        }
        if (player.horizontalCollision || player.verticalCollision) {
            jumppower = maxjump;
        }
        if (jumppower < 0) {
            jumppower = 0;
        }

        return jumppower;
    }

    public void doJump(Entity player, double jumppower, Vec averagemotiontowards, double min_spherevec_dist) {
        if (jumppower > 0) {
            if (GrappleSettings.INSTANCE.getRope_jump_at_angle() && min_spherevec_dist > 1) {
                motion.mutableAdd(averagemotiontowards.withMagnitude(jumppower));
            } else {
                if (jumppower > player.getVelocity().y + jumppower) {
                    motion.y = jumppower;
                } else {
                    motion.y += jumppower;
                }
            }
            this.motion.applyAsMotionTo(player);
        }

        this.disable();
        this.updateServerPos();
    }

    public double getJumpPower(Entity player, Vec spherevec, WebEntity hookEntity) {
        double maxjump = GrappleSettings.INSTANCE.getRope_jump_power();
        Vec jump = new Vec(0, maxjump, 0);
        if (spherevec != null && !GrappleSettings.INSTANCE.getRope_jump_at_angle()) {
            jump = jump.project(spherevec);
        }
        double jumppower = jump.y;

        if (spherevec != null && spherevec.y > 0) {
            jumppower = 0;
        }
        if ((hookEntity != null) && hookEntity.getRopeLength() < 1 && (player.getPos().y < hookEntity.getPos().y)) {
            jumppower = maxjump;
        }

        jumppower = this.getJumpPower(player, jumppower);

        double current_speed = GrappleSettings.INSTANCE.getRope_jump_at_angle() ? -motion.distanceAlong(spherevec) : motion.y;
        if (current_speed > 0) {
            jumppower = jumppower - current_speed;
        }

        if (jumppower < 0) {
            jumppower = 0;
        }

        return jumppower;
    }

    public Vec dampenMotion(Vec motion, Vec forward) {
        Vec newmotion = motion.project(forward);
        double dampening = 0.05;
        return newmotion.scale(dampening).add(motion.scale(1 - dampening));
    }

    public void updateServerPos() {
        //NetworkManager.packetToServer(new PlayerMovementMessage(this.entityId, this.entity.getPos().x, this.entity.getPos().y, this.entity.getPos().z, this.entity.getVelocity().x, this.entity.getVelocity().y, this.entity.getVelocity().z));
    }

    // Vector stuff:

    public void receiveGrappleDetach() {
        this.disable();
    }

    public void receiveEnderLaunch(double x, double y, double z) {
        this.motion.mutableAdd(x, y, z);
        this.motion.applyAsMotionTo(this.entity);
    }

    public void applyAirFriction() {
        double dragforce = 1 / 200F;
        if (this.entity.isTouchingWater() || this.entity.isInLava()) {
            dragforce = 1 / 4F;
        }

        double vel = this.motion.length();
        dragforce = vel * dragforce;

        Vec airfric = new Vec(this.motion.x, this.motion.y, this.motion.z);
        airfric.mutableSetMagnitude(-dragforce);
        this.motion.mutableAdd(airfric);
    }

    public void applyPlayerMovement() {
        entity.sendMessage(Text.of("Applying Player Movement"));
        Vec additionalMotion = this.playerMovement.withMagnitude(0.015 + this.motion.length() * 0.01)
                .scale(this.playerMovementMult);
        this.motion.mutableAdd(additionalMotion);
    }

    public void addHookEntity(WebEntity hookEntity) {
        this.grapplehookEntities.add(hookEntity);
        hookEntity.setRopeLength(hookEntity.getSegmentHandler().getDist(Vec.positionVec(hookEntity), Vec.positionVec(entity).add(new Vec(0, entity.getStandingEyeHeight(), 0))));
        this.grapplehookEntityIds.add(hookEntity.getId());
    }


    // repel stuff
    public Vec checkRepel(Vec p, World w) {
        Vec centerOfMass = p.add(0.0, 0.75, 0.0);
        Vec repelForce = new Vec(0, 0, 0);

        double t = (1.0 + Math.sqrt(5.0)) / 2.0;

        BlockPos pos = BlockPos.ofFloored(p.x, p.y, p.z);

        if (hasBlock(pos, w)) {
            repelForce.mutableAdd(0, 1, 0);

        } else {
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(-1, t, 0), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(1, t, 0), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(-1, -t, 0), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(1, -t, 0), w));

            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(0, 1, t), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(0, -1, t), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(0, -1, -t), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(0, 1, -t), w));

            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(t, 0, -1), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(t, 0, 1), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(-t, 0, -1), w));
            repelForce.mutableAdd(this.castRepelForceRay(centerOfMass, new Vec(-t, 0, 1), w));
        }

        if (repelForce.length() > this.repelMaxPush) {
            repelForce.mutableSetMagnitude(this.repelMaxPush);
        }

        return repelForce;
    }

    public Vec castRepelForceRay(Vec origin, Vec direction, World w) {
        for (double i = 0.5; i < 10; i += 0.5) {
            Vec v2 = direction.withMagnitude(i);
            BlockPos pos = BlockPos.ofFloored(origin.x + v2.x, origin.y + v2.y, origin.z + v2.z);

            if (!this.hasBlock(pos, w))
                continue;

            Vec v3 = new Vec(pos)
                    .mutableSub(origin)
                    .add(0.5D, 0.5D, 0.5D);

            return v3.mutableSetMagnitude(-1 / Math.pow(v3.length(), 2));
        }

        return new Vec(0, 0, 0);
    }

    public boolean hasBlock(BlockPos pos, World w) {
        BlockState blockstate = w.getBlockState(pos);
        return !blockstate.isAir();
    }

    public void receiveGrappleDetachHook(int hookid) {
        if (this.grapplehookEntityIds.contains(hookid)) {
            this.grapplehookEntityIds.remove(hookid);

        } else {
            Manager.INSTANCE.getLogger().warn("Error: controller received hook detach, but hook id not in grapplehookEntityIds");
        }

        WebEntity hookToRemove = null;
        for (WebEntity hookEntity : this.grapplehookEntities) {
            if (hookEntity.getId() == hookid) {
                hookToRemove = hookEntity;
                break;
            }
        }

        if (hookToRemove != null) {
            this.grapplehookEntities.remove(hookToRemove);
        } else {
            Manager.INSTANCE.getLogger().warn("Error: controller received hook detach, but hook entity not in grapplehookEntities");
        }
    }


    public Vec getNearbyWall(Vec tryFirst, Vec trySecond, double extra) {
        float entityCollisionWidth = this.entity.getWidth();

        Vec[] directions = new Vec[]{
                tryFirst,
                trySecond,
                tryFirst.scale(-1),
                trySecond.scale(-1)
        };

        for (Vec direction : directions) {
            Vec collisionRayLength = direction.withMagnitude(entityCollisionWidth / 2 + extra);
            BlockHitResult raytraceresult = GrappleModUtils.INSTANCE.rayTraceBlocks(
                    this.entity,
                    this.entity.getWorld(),
                    Vec.positionVec(this.entity),
                    Vec.positionVec(this.entity).add(collisionRayLength)
            );

            if (raytraceresult != null) {
                this.wallrunRaytraceResult = raytraceresult;
                return direction;
            }
        }

        return null;
    }

    public Vec getWallDirection() {
        Vec tryfirst = new Vec(0, 0, 0);
        Vec trysecond = new Vec(0, 0, 0);

        if (Math.abs(this.motion.x) > Math.abs(this.motion.z)) {
            tryfirst.x = (this.motion.x > 0) ? 1 : -1;
            trysecond.z = (this.motion.z > 0) ? 1 : -1;
        } else {
            tryfirst.z = (this.motion.z > 0) ? 1 : -1;
            trysecond.x = (this.motion.x > 0) ? 1 : -1;
        }

        return getNearbyWall(tryfirst, trysecond, 0.05);
    }

    public Vec getCorner(int cornernum, Vec facing, Vec sideways) {
        Vec corner = new Vec(0, 0, 0);
        if (cornernum / 2 == 0) {
            corner.mutableAdd(facing);
        } else {
            corner.mutableAdd(facing.scale(-1));
        }

        if (cornernum % 2 == 0) {
            corner.mutableAdd(sideways);
        } else {
            corner.mutableAdd(sideways.scale(-1));
        }
        return corner;
    }

    public boolean wallNearby(double dist) {
        float entitywidth = this.entity.getWidth();
        Vec v1 = new Vec(entitywidth / 2 + dist, 0, 0);
        Vec v2 = new Vec(0, 0, entitywidth / 2 + dist);

        for (int i = 0; i < 4; i++) {
            Vec corner1 = getCorner(i, v1, v2);
            Vec corner2 = getCorner((i + 1) % 4, v1, v2);

            BlockHitResult raytraceresult = GrappleModUtils.INSTANCE.rayTraceBlocks(this.entity, this.entity.getWorld(), Vec.positionVec(this.entity).add(corner1), Vec.positionVec(this.entity).add(corner2));
            if (raytraceresult != null) {
                return true;
            }
        }

        return false;
    }

    /*public boolean isWallRunning() {
        double currentSpeed = Math.sqrt(Math.pow(this.motion.x, 2) + Math.pow(this.motion.z, 2));
        if (currentSpeed <= GrappleModLegacyConfig.getConf().enchantments.wallrun.wallrun_min_speed) {
            this.isOnWall = false;
            return false;
        }

        if (this.isOnWall) {
            GrappleModClient.get().setWallrunTicks(GrappleModClient.get().getWallrunTicks() + 1);
        }

        if (GrappleModClient.get().getWallrunTicks() < GrappleModLegacyConfig.getConf().enchantments.wallrun.max_wallrun_time * 40) {
            if (!(this.playerSneak)) {
                // continue wallrun
                if (this.isOnWall && !this.entity.isOnGround() && this.entity.horizontalCollision) {
                    return !(entity instanceof LivingEntity living && living.onClimbable());
                }

                // start wallrun
                if (GrappleModClient.get().isWallRunning(this.entity, this.motion)) {
                    this.isOnWall = true;
                    return true;
                }
            }

            this.isOnWall = false;
        }

        if (GrappleModClient.get().getWallrunTicks() > 0 && (this.entity.isOnGround() || (!this.entity.horizontalCollision && !this.wallNearby(0.2)))) {
            this.ticksSinceLastWallrunSoundEffect = 0;
        }

        return false;
    }*/

    /*public boolean applyWallRun() {
        boolean isWallRunning = this.isWallRunning();

        if (this.playerJump) {
            if (isWallRunning)
                return false;

            this.playerJump = false;
        }

        if (isWallRunning && !GrappleKey.DETACH.isPressed()) {

            Vec wallSide = this.getWallDirection();

            if (wallSide != null)
                this.wallDirection = wallSide;

            if (this.wallDirection == null)
                return false;

            if (!this.playerJump)
                this.motion.y = 0;

            // drag
            double dragForce = GrappleModLegacyConfig.getConf().enchantments.wallrun.wallrun_drag;
            double speed = this.motion.length();

            if (dragForce > speed)
                dragForce = speed;

            Vec wallFriction = new Vec(this.motion);
            if (wallSide != null)
                wallFriction.removeAlong(wallSide);

            wallFriction.mutableSetMagnitude(-dragForce);
            this.motion.mutableAdd(wallFriction);
            this.ticksSinceLastWallrunSoundEffect++;

            double wallRunningSoundTime = GrappleModLegacyConfig.getClientConf().sounds.wallrun_sound_effect_time_s;
            double wallRunningMaxSpeed = GrappleModLegacyConfig.getConf().enchantments.wallrun.wallrun_max_speed;
            double timeLimit = speed != 0
                    ? wallRunningSoundTime * 20 * wallRunningMaxSpeed / speed
                    : -1;

            if (timeLimit < 0 || this.ticksSinceLastWallrunSoundEffect > timeLimit) {
                if (this.wallrunRaytraceResult != null) {
                    BlockPos blockpos = this.wallrunRaytraceResult.getBlockPos();

                    BlockState blockState = this.entity.getWorld().getBlockState(blockpos);
                    Block blockIn = blockState.getBlock();

                    SoundType soundtype = blockIn.getSoundGroup(blockState);

                    this.entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.30F * GrappleModLegacyConfig.getClientConf().sounds.wallrun_sound_volume, soundtype.getPitch());
                    this.ticksSinceLastWallrunSoundEffect = 0;
                }
            }
        }

        // jump
        boolean isDetachRequested = GrappleKey.DETACH.isPressed();
        boolean shouldJump = isDetachRequested && this.isOnWall && !this.playerJump;
        this.playerJump = isDetachRequested && this.isOnWall;

        if (shouldJump && isWallRunning) {
            GrappleModClient.get().setWallrunTicks(0);
            Vec jump = new Vec(0, GrappleModLegacyConfig.getConf().enchantments.wallrun.wall_jump_up, 0);

            if (this.wallDirection != null) {
                double wallJumpSide = GrappleModLegacyConfig.getConf().enchantments.wallrun.wall_jump_side;
                Vec wallDir = this.wallDirection.scale(-wallJumpSide);
                jump.mutableAdd(wallDir);
            }

            this.motion.mutableAdd(jump);

            isWallRunning = false;

            GrappleModClient.get().playWallrunJumpSound();
        }

        return isWallRunning;
    }

    public Vec wallrunPressAgainstWall() {
        // press against wall
        if (this.wallDirection != null) {
            return this.wallDirection.withMagnitude(0.05);
        }
        return new Vec(0, 0, 0);
    }

    public void doDoubleJump() {
        if (-this.motion.y > GrappleModLegacyConfig.getConf().enchantments.doublejump.dont_doublejump_if_falling_faster_than) {
            return;
        }

        if (this.motion.y < 0 && !GrappleModLegacyConfig.getConf().enchantments.doublejump.doublejump_relative_to_falling) {
            this.motion.y = 0;
        }

        this.motion.y += GrappleModLegacyConfig.getConf().enchantments.doublejump.doublejumpforce;
        this.motion.applyAsMotionTo(this.entity);
        this.entity.resetFallDistance();
    }

    public void applySlidingFriction() {
        double dragForce = GrappleModLegacyConfig.getConf().enchantments.slide.sliding_friction;

        if (dragForce > this.motion.length()) {
            dragForce = this.motion.length();
        }

        Vec airFriction = new Vec(this.motion.x, this.motion.y, this.motion.z);
        airFriction.mutableSetMagnitude(-dragForce);
        this.motion.mutableAdd(airFriction);
    }

    public void doSlidingJump() {
        this.motion.y = GrappleModLegacyConfig.getConf().enchantments.slide.slidingjumpforce;
    }

    public void resetRocketProgression() {
        this.rocketKeyDown = true;
        this.rocketProgression = 1.0F;
    }

    public double getRocketProgression() {
        return this.rocketProgression;
    }

    public CustomizationVolume getCurrentCustomizations() {
        return this.custom;
    }

    public void overrideCustomizations(CustomizationVolume volume) {
        this.custom = volume;
    }

    public boolean isRocketKeyDown() {
        return this.rocketKeyDown;
    }

    public boolean isControllerActive() {
        return this.isControllerActive;
    }

    public Vec getCopyOfMotion() {
        return new Vec(this.motion);
    }

    public int getDuplicates() {
        return this.duplicates;
    }

    public boolean areControlsOverridenByEquipment() {
        if (this.custom == null) return false;
        return this.custom.get(IS_EQUIPMENT_OVERRIDE.get());
    }*/
}
