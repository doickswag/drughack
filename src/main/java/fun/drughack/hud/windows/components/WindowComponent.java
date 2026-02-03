package fun.drughack.hud.windows.components;

import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.animations.Animation;
import lombok.*;

@Getter @Setter
public abstract class WindowComponent extends Component {
	protected Animation animation;

	public WindowComponent(String name) {
		super(name);
	}
}