package game.pantyteam;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import ecs.Mappers;
import ecs.components.PhysicsComponent;
import ecs.components.PositionComponent;
import ecs.components.TextureComponent;
import ecs.systems.RenderSystem;

public class PlayScreen implements Screen {

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private FitViewport viewport;

	private Stage stage;
	private Skin skin; // use skin, if i can

	private World world;
	private Box2DDebugRenderer debugRenderer;

	TiledMap tiledMap;
	TiledMapRenderer tiledMapRenderer;

	MapLayer platformLayer;
	MapObjects platforms;

	Engine engine;

	public PlayScreen(SpriteBatch batch) {
		this.batch = batch;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera();
		viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
		viewport.apply();

		stage = new Stage(new ScreenViewport(), batch);
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		// table.setFillParent(true);
		table.setPosition(25, 20);
		stage.addActor(table);

		table.setDebug(true);

		skin = new Skin();

		LabelStyle labelStyle = new LabelStyle(new BitmapFont(), Color.WHITE);
		Label label1 = new Label("Health", labelStyle);

		table.add(label1);

		Box2D.init();
		world = new World(new Vector2(0, -10), true);
		debugRenderer = new Box2DDebugRenderer();

		tiledMap = new TmxMapLoader().load("map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);

		MapProperties prop = tiledMap.getProperties();
		camera.position.set(prop.get("width", Integer.class) * prop.get("tilewidth", Integer.class) / 2,
				prop.get("height", Integer.class) * prop.get("tileheight", Integer.class) / 2, 0);
		camera.update();
		tiledMapRenderer.setView(camera);

		platformLayer = tiledMap.getLayers().get("platforms");
		platforms = platformLayer.getObjects();

		BodyDef groundBodyDef = new BodyDef();
		RectangleMapObject rect = ((RectangleMapObject) platforms.get("ground"));
		groundBodyDef.position.set(rect.getRectangle().x, rect.getRectangle().y);
		Body groundBody = world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();

		groundBox.setAsBox(rect.getRectangle().width / 2, rect.getRectangle().height / 2);
		groundBody.createFixture(groundBox, 0);
		groundBox.dispose();
		groundBody.setTransform(rect.getRectangle().x + rect.getRectangle().width / 2, rect.getRectangle().y + rect.getRectangle().height / 2, groundBody.getAngle());

		engine = new Engine();
		Entity player = new Entity();
		player.add(new PositionComponent(500, 500));
		player.add(new PhysicsComponent(world, Mappers.position.get(player).x, Mappers.position.get(player).y));
		player.add(new TextureComponent("images/first_girl.png"));
		engine.addEntity(player);

		engine.addSystem(new RenderSystem(batch));
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		engine.update(delta);

		batch.setProjectionMatrix(camera.combined);

		tiledMapRenderer.render();

		batch.begin();
		batch.end();

		debugRenderer.render(world, camera.combined);

		stage.act();
		stage.draw();

		world.step(1 / 45f, 6, 2);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
	}

}
