<script>
    export let title = 'Remove icon';
    export let size = 8;
    import {createEventDispatcher} from 'svelte';

    const dispatch = createEventDispatcher();
    const onClick = () => {
        dispatch('click');
        console.log("EEEEE")
    }
</script>

<!--TODO: move to button-->
<span class="remove-container" on:click={onClick}>
        <svg xmlns="http://www.w3.org/2000/svg" x="0px"
             y="0px" width={size} height={size} viewBox="0 0 503.021 503.021"
             style="enable-background:new 0 0 503.021 503.021;"
             xml:space="preserve">

        <title>{title}</title>
            <g>
                <g>
                    <path d="M491.613,75.643l-64.235-64.235c-15.202-15.202-39.854-15.202-55.056,0L251.507,132.222L130.686,11.407    c-15.202-15.202-39.853-15.202-55.055,0L11.401,75.643c-15.202,15.202-15.202,39.854,0,55.056l120.821,120.815L11.401,372.328    c-15.202,15.202-15.202,39.854,0,55.056l64.235,64.229c15.202,15.202,39.854,15.202,55.056,0l120.815-120.814l120.822,120.814    c15.202,15.202,39.854,15.202,55.056,0l64.235-64.229c15.202-15.202,15.202-39.854,0-55.056L370.793,251.514l120.82-120.815    C506.815,115.49,506.815,90.845,491.613,75.643z"/>
                </g>
            </g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
            <g></g>
    </svg>
</span>

<style lang="scss">
    .remove-container {
        //margin-left: 0.5rem;

            & svg {
                fill: #500808;
                opacity: 0.2;
            }

            &:hover {
                & svg {
                    opacity: 0.75;
                }
            }
    }
</style>
