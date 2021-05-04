const routes = {
    getRhymes: 'getRhymes'
};

const URL = process.env.API_URL;

export function getRoute(name) {
    return `${URL}/${routes[name]}`
}