package net.jmecn.snake.server;

import java.util.LinkedList;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

import net.jmecn.snake.core.SnakeConstants;

public class Snake {

	protected String name;
	protected LinkedList<Entity> bodys;
	
	protected float collisionRadius;
	protected int length;
	protected float speed;
	
	protected boolean isSpeedUp;
	protected boolean isDead;
	
	public Snake(String name) {
		this.name = name;
		bodys = new LinkedList<Entity>();
		collisionRadius = SnakeConstants.snakeBodyRadius;
		length = 0;
		speed = SnakeConstants.speed;
		isSpeedUp = false;
		isDead = false;
	}
	
	/**
	 * 调整蛇身每个位置的运动方向，使其紧随前一截身体。
	 */
	public void follow() {
		TempVars tmp = TempVars.get();
		int len = bodys.size();
		for(int i=len; i>0; i--) {
			Entity last = bodys.get(len);
			Entity front = bodys.get(len - 1);
			
			// Follow
			float maxDist = collisionRadius;
			
			Vector3f loc1 = last.getLocation();
			Vector3f loc2 = front.getLocation();
	        double dx = loc2.x - loc1.x;
	        double dy = loc2.y - loc1.y;
	        double distSquared = dx * dx + dy * dy;
	        
			if (distSquared > maxDist * maxDist) {
				Vector3f linear = tmp.vect1;
				linear.set((float)dx, (float)dy, 0f);
				linear.normalizeLocal();
				last.setLinear(linear);
			}
		}
		tmp.release();
	}
	
	/**
	 * 计算位移
	 * @param time
	 */
	public void move(float time) {
		
		float delta = speed * time;
		if (isSpeedUp) {// 玩家按下加速键
			delta *= 2;
		}
		
		int len = bodys.size();
		for(int i=len; i>0; i--) {
			Entity e = bodys.get(len);
			
			Vector3f loc = e.getLocation();
			Vector3f linear = e.getLinear();
			
			loc.addLocal(linear.x * delta, linear.y * delta, 0);

			// 让模型的脸朝向线速度的方向
			getFacing(linear.x, linear.y, e.getFacing());
		}
	}

	/**
	 * 在XOY平面上，根据线速度的方向，计算图片当前的朝向。
	 * 
	 * <pre>
	 * (x*x + y*y) == 1
	 * vec3 xAxis = (y, -x, 0)
	 * vec3 yAxis = (x, y, 0)
	 * vec3 zAxis = (0, 0, 1)
	 * 
	 * Matrix3 rot = (
	 * y,  x,  0,
	 * -x, y,  0,
	 * 0,  0,  1)
	 * </pre>
	 *
	 * @see <code>com.jme3.math.Quaternion.fromRotationMatrix</code>
	 * @param x
	 * @param y
	 * @return
	 */
	protected Quaternion getFacing(float x, float y, Quaternion result) {
		// normalize
		float length = x * x + y * y;
		if (length != 1f && length != 0f) {
			length = 1f / FastMath.sqrt(length);
			x *= length;
			y *= length;
		}
		
		float z = 0, w = 1;
		float t = y + y + 1;
		if (t >= 0) {
			float s = FastMath.sqrt(t + 1);
			w = 0.5f * s;
			s = 0.5f / s;
			z = -2 * x * s;
		} else {
			float s = FastMath.sqrt(3f - t);
			z = s * 0.5f;
			s = 0.5f / s;
			w = -2 * x * s;
		}

		result.set(0, 0, z, w);

		return result;
	}
}
