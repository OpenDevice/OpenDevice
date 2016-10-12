/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package opendevice.io.tests.util;

import java.util.Timer;
import java.util.TimerTask;

public class ResetableTimer extends Timer
{
    private Runnable  task;
    private TimerTask timerTask;

    public void schedule(Runnable runnable, long delay)
    {
        task = runnable;
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                task.run();
            }
        };
        this.schedule(timerTask, delay);
    }

    public void reschedule(long delay)
    {
        timerTask.cancel();
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                task.run();
            }
        };
        this.schedule(timerTask, delay);
    }
}