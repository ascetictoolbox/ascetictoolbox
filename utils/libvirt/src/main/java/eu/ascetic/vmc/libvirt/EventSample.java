package eu.ascetic.vmc.libvirt;

class EventSample {
	volatile static boolean keepGoing = true;

	public static void main(String[] args) throws LibvirtException {
		Connect c = null;
		try {
			Connect.initEventLoop();
			c = new Connect("qemu:///system");

			c.setKeepAlive(3, 10);
			int cb1 = c.domainEventRegister(new Connect.DomainEvent.LifecycleCallback() {
						@Override
						public void onLifecycleChange(
								Connect c,
								Domain d,
								Connect.DomainEvent.LifecycleCallback.Event event,
								int detail) {

							System.out.println("lifecycle change: " + d + " "
									+ event + " " + detail);
						}
					});

			System.out.println("Press Ctrl+D to exit.\n");

			new Thread() {
				@Override
				public void run() {
					try {
						while (System.in.read() > 0)
							;
					} catch (java.io.IOException e) {
					}
					keepGoing = false;
				}
			}.start();

			while (keepGoing && c.isAlive())
				c.processEvent();

			c.domainEventDeregister(cb1);
		} finally {
			if (c != null)
				c.close();
		}
	}
}
