package Engine;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.Clip;

import Graphics.Painter;
import Graphics.ResourceLoader;
import Units.Asteroid;
import Units.Bullet;
import Units.Dragon;
import Units.DummyUnit;
import Units.Enemy;
import Units.EnemyBullet;
import Units.Missile;
import Units.Player;
import Units.Unit;
import core.IObserver;

public class Engine implements IObserver, Runnable {
	
	double playerSpeed = 4;
	double bulletSpeed = 20;
	double missileSpeed = 5;
	int fireRate = 75;
	
	final Random r = new Random();
	double enemyFireProbability = 0.01;
	
	Dimension panelSize;
	public static Rectangle bounds; // same size as panelSize
	Painter painter;
	
	public ConcurrentLinkedQueue<Missile> missiles;
	public ConcurrentLinkedQueue<Bullet> bullets;
	
	public ConcurrentLinkedQueue<Unit> dummyUnits;
	
	public ConcurrentLinkedQueue<Asteroid> asteroids;
	
	public ConcurrentLinkedQueue<Enemy> enemies;
	public ConcurrentLinkedQueue<Unit> staticUnits;
	
	public Dragon dragon;
	
	public Player player;
	
	Point mouse = new Point();
	
	boolean[] keyCodes = new boolean[256];
	boolean mousePressed = false;
	boolean rightPressed = false;
	boolean leftPressed = false;
	
	ResourceLoader res;

//	SimpleRenderer simpleRenderer;
//	NullRenderer nullRenderer;
//	ImageRenderer imageRenderer;
	
	public Engine(Dimension panelSize) {
		this.panelSize = panelSize;
		Engine.bounds = new Rectangle(0, 0, panelSize.width, panelSize.height);
		res = new ResourceLoader();
		
		player = new Player(new Vector(600, 400), null, res.getPlayerImage(), mouse);
		
		missiles = new ConcurrentLinkedQueue<Missile>();
		bullets = new ConcurrentLinkedQueue<Bullet>();
		
		dummyUnits = new ConcurrentLinkedQueue<Unit>();
		
		asteroids = new ConcurrentLinkedQueue<Asteroid>();
			
		
		enemies = new ConcurrentLinkedQueue<Enemy>();
		staticUnits = new ConcurrentLinkedQueue<Unit>();
		
		
		// listeners
		MousepadListener ml = new MousepadListener();
		KeyboardListener kl = new KeyboardListener();
		ml.addObserver(this);
		kl.addObserver(this);
		

		enemies.add(new Enemy(
			new Vector(50, 50),
			null,
			null,
			res.getEnemyImage(),
			player
		));
		
		Unit dummy = new DummyUnit(new Vector(300, 300), new Dimension(50, 40), null);
		//dummyUnits.add(dummy);
		
		Asteroid a = new Asteroid(new Vector(300, 300), null, res.getAsteroidImage());
		asteroids.add(a);
		
		dragon = new Dragon(new Vector(0, 0), null, 500, res.getDragonHeadImage(), res.getDragonBodyImage(), res.getDragonTailImage());
		
		painter = new Painter(this, panelSize, ml, kl);
	}
	
	
	@Override
	public void run() {
		while(true)
		{
			movePlayer();
			moveUnits();
			
			sleep(10);
		}
	}
	
	private Unit getClosestEnemy(Point location) {
		if (enemies.isEmpty() && asteroids.isEmpty()) return null;
		
		Unit closest = null;
		if (enemies.isEmpty()) closest = asteroids.element();
		else closest = enemies.element();
		
		double minDistance = Double.MAX_VALUE;
		for (Unit u : enemies) {
			double newDistance = u.getLocation().distance(new Vector(location.getX(), location.getY()));
			if (newDistance < minDistance) {
				minDistance = newDistance;
				closest = u;
			}
		}
		for (Unit u : asteroids) {
			double newDistance = u.getLocation().distance(new Vector(location.getX(), location.getY()));
			if (newDistance < minDistance) {
				minDistance = newDistance;
				closest = u;
			}
		}
		System.out.println(asteroids.size());
		System.out.println("returning " + closest.toString());
		return closest;
	}
	
	
	private void moveUnits() {		
		// if bullet is type enemy bullet, then don't remove it
		// because it might still be in enemies hitbox (where its created)

		// move enemies and check collisions
		enemies.forEach(e -> {
			e.move();
			bullets.forEach(b -> {
				// reduce hp remove if unit is dead
				if (e.checkCollision(b) && !(b instanceof EnemyBullet)) {
					bullets.remove(b);
					// TODO: set bullet dmg and use it here!
					if (e.lowerHealth(5)) {
						enemies.remove(e);
					}
				}
			});
			missiles.forEach(m -> {
				// TODO: same here as for bullets
				if (e.checkCollision(m)) {
					missiles.remove(m);
					if (e.lowerHealth(50)) {
						enemies.remove(e);
					}
				}
			});
			if (r.nextDouble() < enemyFireProbability) e.shoot(res, bullets);
			if (e.isOutOfBounds()) e.reposition();
		});
		
		// bullet collisions
		bullets.forEach(b -> {
			if (player.checkCollision(b) && b instanceof EnemyBullet) {
				bullets.remove(b);
				// TODO: var for bullet dmg
				if (player.lowerHealth(5)) {
					// TODO: player out of hp
				}
			}
			
			asteroids.forEach(a -> {
				if (b.checkCollision(a)) {
					bullets.remove(b);
					asteroids.remove(a);
				}
			});
			
		});
		
		// move bullets
		bullets.forEach(u -> {
			u.move();
			if (u.isOutOfBounds()) bullets.remove(u);
		});
		// move missiles
		missiles.forEach(m -> {
			m.move();
			if (m.checkCollision(m.target)) {
				missiles.remove(m);
				deleteTarget(m.target);
			}
			
			// if target is dead, find another
			// otherwise just remove
			// TODO: don't just remove it?
			if (!isTargetAlive(m.target)) {
				Unit target = getClosestEnemy(mouse);
				if (target != null) m.target = target;
				else missiles.remove(m);
			}
		});
		
		asteroids.forEach(a -> {
			a.move();
			if (a.isOutOfBounds()) a.reposition();
		});
		
		dragon.move();
		dragon.checkCollision(bullets, asteroids);
	}
	
	private void deleteTarget(Unit target) {
		enemies.remove(target);
		asteroids.remove(target);
	}
	
	private void movePlayer() {
		Vector force = new Vector(0, 0);
		// W
		if (keyCodes[87]) force.y += -playerSpeed;
		// A
		if (keyCodes[65]) force.x += -playerSpeed;
		// S
		if (keyCodes[83]) force.y +=  playerSpeed;
		// D
		if (keyCodes[68]) force.x +=  playerSpeed;
		
		player.updateDirection();
		player.updatePosition(force);
	}
	
	
	Thread bulletLauncher = new Thread();
	private void createBullet() {
		// spawn bullets until mouse is released
		// fire every *fireRate* milliseconds
		if (bulletLauncher.isAlive()) return;
		
		bulletLauncher = new Thread(() -> {
			while(leftPressed) {
				Vector direction = new Vector(
					mouse.x - player.getLocation().x,
					mouse.y - player.getLocation().y
				);
				direction.norm();
				direction.multi(bulletSpeed);
				
				Bullet b = new Bullet(
					new Vector(
						player.centerposition.x - res.getBulletImage().getWidth(null)/2,
						player.centerposition.y - res.getBulletImage().getHeight(null)/2
					),
					direction,
					null,
					res.getBulletImage()
				);
				b.setUnitImage(res.getBulletImage());
				bullets.add(b);
				sleep(fireRate);
			}
		});
		bulletLauncher.start();
	}
	
	Thread missileLauncher = new Thread();
	private void createMissile(Unit target) {
		// if thread responsible is already up and running,
		// it means a new one shouldn't be created and run
		if (missileLauncher.isAlive()) return;
		
		missileLauncher = new Thread(() -> {
			while(rightPressed) {
				//if (!isTargetAlive(target)) continue;
				Missile m = new Missile(
					new Vector(
						player.getHitbox().getCenterX() - res.getMissileImage().getWidth(null)/2,
						player.getHitbox().getCenterY() - res.getMissileImage().getHeight(null)/2
					),
					null,
					res.getMissileImage(),
					target
				);
				missiles.add(m);
				System.out.println("missile launcher alive");
				sleep(500);
			}
		});
		missileLauncher.start();
	}
	
	public boolean isTargetAlive(Unit target) {
		if (enemies.contains(target)) return true;
		return asteroids.contains(target);
	}
	
	
	@Override
	public void notifyMouseMoved(Point location) {
		mouse.setLocation(location);;
	}
	
	@Override
	public void notifyMousePressed(MouseEvent event) {
		mousePressed = !mousePressed;
	}
	
	@Override
	public void notifyMouseReleased(MouseEvent event) {
		
	}
	
	@Override
	public void notifyKeysPressed(boolean[] keyCodes) {
		this.keyCodes = keyCodes;
	}
	
	@Override
	public void notifyCharacterKeyPressed(Set<Character> keys) {}
	
//	public void addPlayer(Player player) {
//		this.player = player;
//	}
	
//	public void addMovableUnit(MovableUnit unit) {
//		movableUnits.add(unit);
//	}
//	
//	public void addMovableUnit(IStaticUnit unit) {
//		staticUnits.add(unit);
//	}

	public static void sleep(int t) {
		try { Thread.sleep(t); } 
		catch (InterruptedException e) {e.printStackTrace(); }
	}

	public void notifyRightPress(Point location) { 
		rightPressed = !rightPressed;
		// dont spawn if there are no enemies
		if (enemies.isEmpty() && asteroids.isEmpty()) return;
		createMissile(getClosestEnemy(mouse));
	}
	
	public void notifyLeftPress(Point location) {
		leftPressed = !leftPressed;
		createBullet();
	}
	
	public void notifyRightRelease(Point location) { rightPressed = !rightPressed; }
	public void notifyLeftRelease(Point location) { leftPressed = !leftPressed; }
}