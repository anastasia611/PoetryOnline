const API_VERSION = 5.131;
const CLIENT_ID = '7876256';
const DISPLAY = 'page';
const REDIRECT = 'http://vk.com/';
const SCOPE = 'friends,wall,groups,pages';
const TYPE = 'token';
const STATE = 123456;

const API_URL = 'http://localhost:8010/proxy'; //'https://api.vk.com';
const AUTH_URL = 'http://localhost:8011/proxy'; //'https://oauth.vk.com';
const TIME_DELAY = 350;


export async function getComments(
    groups,
    postsNum,
    postOffset = 0,
    commentsNum = 100,
    likesMin = 10,
    likesMax = 200,
    bdateMin = new Date('01.01.1992'),
    bdateMax = new Date('01.01.1998')) {

    // let response0 = await fetch(
    //     getUrl(AUTH_URL,
    //         {
    //             client_id: CLIENT_ID,
    //             display: DISPLAY,
    //             redirect_uri: 'http://vk.com',
    //             scope: SCOPE,
    //             response_type: TYPE,
    //             state: STATE
    //         }));
    // console.log(response)
    const token = '5c2b4029231c94ae079e71a38de53ff111295a598baa5b3cc4fb28f6abb859619dedb222e016852b0e4dc';

    let groupIds = [];

    if (!groups || !groups.length) {
        let response = await fetch(
            getAPIUrl('groups.get',
                {},
                token));
        response = await response.json();
        groupIds = response.response.items;
    } else {
        let response = await fetch(
            getAPIUrl('groups.getById',
                {
                    group_ids: groups.join(',')
                },
                token));
        groups = await response.json();
        groupIds = groups.map(g => g.id);
    }

    for (let groupId of groupIds) {
        let response = await fetch(
            getAPIUrl('wall.get',
                {
                    owner_id: -groupId,
                    offset: postOffset,
                    count: postsNum
                },
                token));

        const posts = await response.json();
        if (!posts.response) {
            continue;
        }

        for (let post of posts.response.items) {
            await delay(TIME_DELAY);

            response = await fetch(
                getAPIUrl('wall.getComments',
                    {
                        owner_id: -groupId,
                        count: commentsNum,
                        post_id: post.id,
                        need_likes: 1,
                        sort: 'asc'
                    },
                    token));

            const comments = await response.json();
            if (!comments.response) {
                continue;
            }

            for (let comment of comments.response.items) {
                if (!comment.likes) {
                    continue;
                }
                const likes = comment.likes.count;

                if (!comment.likes.user_likes && likes >= likesMin && likes <= likesMax) {
                    await delay(TIME_DELAY);

                    response = await fetch(
                        getAPIUrl('users.get',
                            {
                                user_ids: comment.from_id,
                                fields: 'bdate,sex,has_photo,city,home_town',
                            },
                            token));

                    const users = await response.json();
                    if (!users.response) {
                        continue;
                    }

                    const user = users.response[0];
                    if (!user.bdate) {
                        continue;
                    }
                    const ymd = user.bdate.split('.');
                    const userBDate = new Date(ymd[2], ymd[1], ymd[0]);
                    if (user.sex == '2'
                        && (user.city && user.city.title === 'Санкт-Петербург' || user.home_town === 'Санкт-Петербург')
                        && userBDate.getTime() >= bdateMin.getTime()
                        && userBDate.getTime() <= bdateMax.getTime()) {

                        response = await fetch(
                            getAPIUrl('likes.add',
                                {
                                    type: 'comment',
                                    owner_id: -groupId,
                                    item_id: comment.id
                                },
                                token));
                        console.log(user, comment.text);
                    }
                }
            }
        }
    }
    console.log('------Done------')
}

function getUrl(url = API_URL, params) {
    params = {
        ...params,
        v: API_VERSION
    };
    const paramsStr = Object.keys(params).map(p => `${p}=${params[p]}&`).join('&');
    return `${url}/?${paramsStr}`;
}

function getAPIUrl(method, params, token) {
    params = {
        ...params,
        access_token: token
    };
    return getUrl(`${API_URL}/method/${method}`, params)
}

function delay(millis) {
    return new Promise(resolve => {
        setTimeout(() => resolve(), millis);
    });
}