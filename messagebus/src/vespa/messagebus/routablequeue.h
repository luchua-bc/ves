// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "imessagehandler.h"
#include "ireplyhandler.h"
#include "queue.h"
#include "routable.h"
#include "message.h"
#include "reply.h"
#include <mutex>
#include <condition_variable>

namespace mbus {

/**
 * A RoutableQueue is a queue of Routable objects that also implements
 * the IMessageHandler and IReplyHandler APIs. This class is included
 * as a convenience for application developers that does not want to
 * write their own Message and Reply handlers for use with session
 * objects. Note that a RoutableQueue cannot be copied and that it
 * owns all objects currently on the queue. The RoutableQueue class is
 * thread-safe.
 **/
class RoutableQueue : public IMessageHandler,
                      public IReplyHandler
{
private:
    std::mutex              _lock;
    std::condition_variable _cond;
    Queue<Routable*>        _queue;

public:
    /**
     * Create an empty queue.
     **/
    RoutableQueue();

    /**
     * The destructor will delete any objects still on the queue.
     **/
    ~RoutableQueue() override;

    /**
     * Obtain the number of elements currently in this queue. Note
     * that the return value of this method may become invalid really
     * fast if the queue is used by multiple threads.
     *
     * @return current queue size
     **/
    uint32_t size();

    /**
     * Enqueue a routable on this queue.
     *
     * @param r the Routable to enqueue
     **/
    void enqueue(Routable::UP r);

    /**
     * Dequeue a routable from this queue. This method will block if
     * the queue is currently empty. The 'timeout' parameter
     * indicate how long to wait for something to be enqueued. If 0 is
     * given as timeout, this method will not block at all, making it
     * perform a simple poll. If the dequeue operation times out, the
     * returned auto-pointer will point to 0.
     *
     * @return the dequeued routable
     * @param timeout how long to wait if the queue is empty
     **/
    Routable::UP dequeue(duration timeout);
    Routable::UP dequeue() { return dequeue(duration::zero());}

    /**
     * Handle a Message by enqueuing it.
     *
     * @param msg the Message to handle
     **/
    void handleMessage(Message::UP msg) override;

    /**
     * Handle a Reply by enqueuing it.
     *
     * @param reply the Reply to handle
     **/
    void handleReply(Reply::UP reply) override;
};

} // namespace mbus

